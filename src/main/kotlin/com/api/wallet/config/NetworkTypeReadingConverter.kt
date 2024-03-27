package com.api.wallet.config

import com.api.wallet.enums.NetworkType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter


@ReadingConverter
class NetworkTypeReadingConverter : Converter<String, NetworkType> {
    override fun convert(source: String): NetworkType {
        return NetworkType.valueOf(source)
    }
}