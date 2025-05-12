package com.sougata.supplysync.settings.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import com.github.javafaker.Faker
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.util.Inputs
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class SettingsViewModel : ViewModel() {
    private val supplierRepo = SupplierRepository()
    private val customerRepo = CustomerRepository()

    private val faker = Faker(Locale("en", "IN"))
    private val calendar = Calendar.getInstance()
    private var fromDate: Date
    private var toDate: Date

    init {
        this.calendar.set(2025, 0, 1)
        this.fromDate = this.calendar.time

        this.calendar.set(2025, 4, 30)
        this.toDate = this.calendar.time
    }

    fun customer(view: View) {
        repeat(100) {
            this.customerRepo.addCustomer(
                Customer(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    name = this.faker.name().fullName(),
                    receivableAmount = this.faker.number().randomDouble(2, 1000, 100000),
                    dueOrders = this.faker.number().numberBetween(1, 10),
                    phone = this.faker.phoneNumber().phoneNumber(),
                    email = this.faker.internet().emailAddress(),
                    note = this.faker.lorem().sentence(),
                    profileImageUrl = Inputs.getRandomImageUrl()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }

    fun customerPayment(view: View) {
        repeat(100) {
            this.customerRepo.addCustomerPayment(
                CustomerPayment(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    customerId = UUID.randomUUID().toString(),
                    amount = this.faker.number().randomDouble(2, 1000, 100000),
                    paymentTimestamp = Timestamp(
                        this.faker.date().between(this.fromDate, this.toDate)
                    ),
                    note = this.faker.lorem().sentence(),
                    customerName = this.faker.name().fullName()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }

    fun order(view: View) {
        repeat(100) {
            this.customerRepo.addOrder(
                Order(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    userItemId = UUID.randomUUID().toString(),
                    userItemName = this.faker.commerce().productName(),
                    quantity = this.faker.number().numberBetween(1, 10),
                    amount = this.faker.number().randomDouble(2, 1000, 100000),
                    customerId = UUID.randomUUID().toString(),
                    customerName = this.faker.name().fullName(),
                    deliveryTimestamp = Timestamp(
                        this.faker.date().between(this.fromDate, this.toDate)
                    ),
                    delivered = this.faker.bool().bool()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }

    fun orderedItem(view: View) {
        repeat(100) {
            this.supplierRepo.addOrderedItem(
                OrderedItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    supplierItemId = UUID.randomUUID().toString(),
                    supplierItemName = this.faker.commerce().productName(),
                    quantity = this.faker.number().numberBetween(1, 10),
                    amount = this.faker.number().randomDouble(2, 1000, 100000),
                    supplierId = UUID.randomUUID().toString(),
                    supplierName = this.faker.name().fullName(),
                    orderTimestamp = Timestamp(
                        this.faker.date().between(this.fromDate, this.toDate)
                    ),
                    received = this.faker.bool().bool()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }

    fun supplier(view: View) {
        repeat(100) {
            this.supplierRepo.addSupplier(
                Supplier(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    name = this.faker.name().fullName(),
                    dueAmount = this.faker.number().randomDouble(2, 1000, 100000),
                    phone = this.faker.phoneNumber().phoneNumber(),
                    email = this.faker.internet().emailAddress(),
                    note = this.faker.lorem().sentence(),
                    paymentDetails = this.faker.lorem().sentence(),
                    profileImageUrl = Inputs.getRandomImageUrl()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }

    fun supplierItem(view: View) {
        repeat(100) {
            this.supplierRepo.addSupplierItem(
                SupplierItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    name = this.faker.commerce().productName(),
                    price = this.faker.number().randomDouble(2, 1000, 100000),
                    details = this.faker.lorem().sentence()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }


    fun supplierPayment(view: View) {
        repeat(100) {
            this.supplierRepo.addSupplierPayment(
                SupplierPayment(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    amount = this.faker.number().randomDouble(2, 1000, 100000),
                    paymentTimestamp = Timestamp(
                        this.faker.date().between(this.fromDate, this.toDate)
                    ),
                    note = this.faker.lorem().sentence(),
                    supplierId = UUID.randomUUID().toString(),
                    supplierName = this.faker.name().fullName()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }

    fun userItem(view: View) {
        repeat(100) {
            this.customerRepo.addUserItem(
                UserItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = Timestamp.now(),
                    name = this.faker.commerce().productName(),
                    inStock = this.faker.number().numberBetween(1, 100),
                    price = this.faker.number().randomDouble(2, 1000, 100000),
                    details = this.faker.lorem().sentence()
                )
            ) { status, message -> Log.d("TAG", message) }
        }
    }
}