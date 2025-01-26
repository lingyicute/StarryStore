package com.looker.starrystore.network

import com.looker.starrystore.BuildConfig
import com.looker.starrystore.network.header.HeadersBuilder
import com.looker.starrystore.network.header.KtorHeadersBuilder
import com.looker.starrystore.network.validation.FileValidator
import com.looker.starrystore.network.validation.ValidationException
import com.looker.starrystore.utility.common.extension.size
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.etag
import io.ktor.http.isSuccess
import io.ktor.http.lastModified
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.Proxy
import kotlin.coroutines.cancellation.CancellationException

internal class KtorDownloader(
    httpClientEngine: HttpClientEngine,
    private val dispatcher: CoroutineDispatcher,
) : Downloader {

    private var client = client(httpClientEngine)
        set(newClient) {
            field.close()
            field = newClient
        }

    override fun setProxy(proxy: Proxy) {
        client = client(OkHttp.create { this.proxy = proxy })
    }

    override suspend fun headCall(
        url: String,
        headers: HeadersBuilder.() -> Unit
    ): NetworkResponse {
        val headRequest = createRequest(
            url = url,
            headers = headers
        )
        return client.head(headRequest).asNetworkResponse()
    }

    override suspend fun downloadToFile(
        url: String,
        target: File,
        validator: FileValidator?,
        headers: HeadersBuilder.() -> Unit,
        block: ProgressListener?
    ): NetworkResponse = withContext(dispatcher) {
        try {
            val request = createRequest(
                url = url,
                headers = {
                    inRange(target.size)
                    headers()
                },
                fileSize = target.size,
                block = block
            )
            client.prepareGet(request).execute { response ->
                val networkResponse = response.asNetworkResponse()
                if (networkResponse !is NetworkResponse.Success) {
                    return@execute networkResponse
                }
                response.bodyAsChannel().copyTo(target.outputStream())
                validator?.validate(target)
                networkResponse
            }
        } catch (e: SocketTimeoutException) {
            NetworkResponse.Error.SocketTimeout(e)
        } catch (e: ConnectTimeoutException) {
            NetworkResponse.Error.ConnectionTimeout(e)
        } catch (e: IOException) {
            NetworkResponse.Error.IO(e)
        } catch (e: ValidationException) {
            target.delete()
            NetworkResponse.Error.Validation(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            NetworkResponse.Error.Unknown(e)
        }
    }

    private fun client(
        engine: HttpClientEngine = OkHttp.create()
    ): HttpClient {
        return HttpClient(engine) {
            userAgentConfig()
            timeoutConfig()
        }
    }


    private fun createRequest(
        url: String,
        headers: HeadersBuilder.() -> Unit,
        fileSize: Long? = null,
        block: ProgressListener? = null
    ) = request {
        url(url)
        this.headers {
            KtorHeadersBuilder(this).headers()
        }
        onDownload { read, total ->
            if (block != null) {
                block(
                    DataSize(read + (fileSize ?: 0L)),
                    DataSize((total ?: 0L) + (fileSize ?: 0L))
                )
            }
        }
    }
}

private const val CONNECTION_TIMEOUT = 30_000L
private const val SOCKET_TIMEOUT = 15_000L
private const val USER_AGENT = "Droid-ify, ${BuildConfig.VERSION_NAME}"

private fun HttpClientConfig<*>.userAgentConfig() = install(UserAgent) {
    agent = USER_AGENT
}

private fun HttpClientConfig<*>.timeoutConfig() = install(HttpTimeout) {
    connectTimeoutMillis = CONNECTION_TIMEOUT
    socketTimeoutMillis = SOCKET_TIMEOUT
}

private fun HttpResponse.asNetworkResponse(): NetworkResponse =
    if (status.isSuccess() || status == HttpStatusCode.NotModified) {
        NetworkResponse.Success(status.value, lastModified(), etag())
    } else {
        NetworkResponse.Error.Http(status.value)
    }
