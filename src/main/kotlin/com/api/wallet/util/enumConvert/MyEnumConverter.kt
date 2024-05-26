package com.api.wallet.util.enumConvert

import com.api.wallet.enums.MyEnum
import org.springframework.data.r2dbc.convert.EnumWriteSupport

data class MyEnumConverter<T : Enum<T>>(private val enumType: Class<T>) : EnumWriteSupport<MyEnum>() {

}