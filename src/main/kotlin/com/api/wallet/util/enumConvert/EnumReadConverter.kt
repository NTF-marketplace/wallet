package com.api.wallet.util.enumConvert

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.ReadingConverter
import java.util.*

@ReadingConverter
class EnumReadingConverter(private val enumTypes: Set<Class<*>>) : GenericConverter {

    private val convertibleTypes: Set<GenericConverter.ConvertiblePair> = enumTypes.map {
        GenericConverter.ConvertiblePair(String::class.java, it)
    }.toSet()

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> = Collections.unmodifiableSet(convertibleTypes)

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        if (source == null) {
            return null
        }
        if (sourceType.type != String::class.java) {
            return null
        }
        val enumType = targetType.type as Class<out Enum<*>>
        return java.lang.Enum.valueOf(enumType, source as String)
    }
}