package com.looker.starrystore.utility.extension

import androidx.fragment.app.Fragment
import com.looker.starrystore.MainActivity

inline val Fragment.screenActivity: MainActivity
    get() = requireActivity() as MainActivity
