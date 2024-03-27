package com.api.wallet.config

import com.api.wallet.enums.NetworkType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class NetworkTypeWritingConverter : Converter<NetworkType, String> {
    override fun convert(source: NetworkType): String {
        return source.name
    }
}