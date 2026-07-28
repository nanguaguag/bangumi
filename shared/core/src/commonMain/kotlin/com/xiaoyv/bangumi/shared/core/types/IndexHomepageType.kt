package com.xiaoyv.bangumi.shared.core.types

import androidx.annotation.StringDef

@StringDef(
    IndexHomepageType.FEATURED,
    IndexHomepageType.ADVANCE,
    IndexHomepageType.HOT,
    IndexHomepageType.NEWEST
)
@Retention(AnnotationRetention.SOURCE)
annotation class IndexHomepageType {
    companion object {
        const val FEATURED = "featured"
        const val ADVANCE = "advance"
        const val NEWEST = ""
        const val HOT = "collect"
    }
}
