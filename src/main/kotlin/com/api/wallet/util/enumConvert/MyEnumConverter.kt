package com.api.wallet.util.enumConvert

import com.api.wallet.enums.AccountType
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.MyEnum
import com.api.wallet.enums.TransferType
import org.springframework.data.r2dbc.convert.EnumWriteSupport

data class MyEnumConverter<T : Enum<T>>(private val enumType: Class<T>) : EnumWriteSupport<MyEnum>()

data class ChinTypeConvert<T: Enum<T>>(private val enumType: Class<T>): EnumWriteSupport<ChainType>()

data class AccountTypeConvert<T: Enum<T>>(private val enumType: Class<T>): EnumWriteSupport<AccountType>()

data class TransferTypeConvert<T: Enum<T>>(private val enumType: Class<T>): EnumWriteSupport<TransferType>()