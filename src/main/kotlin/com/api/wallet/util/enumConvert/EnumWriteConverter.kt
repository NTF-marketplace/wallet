package com.api.wallet.util.enumConvert

import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.WritingConverter
import java.util.*

@WritingConverter
class EnumWritingConverter(private val enumTypes: Set<Class<*>>) : GenericConverter {

    private val convertibleTypes: Set<GenericConverter.ConvertiblePair> = enumTypes.map {
        GenericConverter.ConvertiblePair(it, String::class.java)
    }.toSet()

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> = Collections.unmodifiableSet(convertibleTypes)

    override fun convert(
        source: Any?,
        sourceType: org.springframework.core.convert.TypeDescriptor,
        targetType: org.springframework.core.convert.TypeDescriptor
    ): Any? {
        if (source == null) {
            return null
        }
        if (!Enum::class.java.isAssignableFrom(sourceType.type)) {
            return null
        }
        return (source as Enum<*>).name
    }
}