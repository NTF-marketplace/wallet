package com.api.wallet.util.enumConvert

import com.api.wallet.enums.*
import org.springframework.data.r2dbc.convert.EnumWriteSupport

data class MyEnumConverter(private val enumType: Class<MyEnum>) : EnumWriteSupport<MyEnum>()

data class ChinTypeConvert(private val enumType: Class<ChainType>): EnumWriteSupport<ChainType>()

data class AccountTypeConvert(private val enumType: Class<AccountType>): EnumWriteSupport<AccountType>()

data class TransferTypeConvert(private val enumType: Class<TransferType>): EnumWriteSupport<TransferType>()

data class StatusTypeConvert(private val enumType: Class<StatusType>): EnumWriteSupport<StatusType>()

data class TransactionStatusTypeConvert(private val enumType: Class<TransaionStatusType>): EnumWriteSupport<TransaionStatusType>()
