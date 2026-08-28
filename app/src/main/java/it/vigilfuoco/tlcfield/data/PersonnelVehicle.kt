package it.vigilfuoco.tlcfield.data

data class Personnel(
    val id: Int,
    val qualification: String,
    val fullName: String,
    val active: Boolean = true
)

data class Vehicle(
    val id: Int,
    val description: String,
    val plate: String,
    val active: Boolean = true
)
