package com.tutaap.tutadriver

class Trucks {
    var Id: Int? = null
    var name: String? = null
    var description: String? = null
    var created_at: String? = null
    var updated_at: String? = null
    var deleted_at: String? = null
    var base_charge: Int? = null
    var price_per_kilometer: Double? = null
    var price_per_second: Double? = null
    var average_speed: Int? = null


    constructor(Id: Int, name: String, description: String, created_at: String, updated_at: String, deleted_at: String, base_charge: Int, price_per_kilometer: Double
                ,price_per_second: Double, average_speed: Int) {
        this.Id = Id
        this.name = name
        this.description = description
        this.created_at = created_at
        this.updated_at = updated_at
        this.deleted_at = deleted_at
        this.base_charge = base_charge
        this.price_per_kilometer = price_per_kilometer
        this.price_per_second = price_per_second
        this.average_speed = average_speed

    }
}