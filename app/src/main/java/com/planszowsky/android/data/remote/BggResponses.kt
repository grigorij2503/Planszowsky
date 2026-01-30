package com.planszowsky.android.data.remote

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "items", strict = false)
data class BggSearchResponse(
    @field:ElementList(inline = true, required = false)
    var items: List<BggSearchItem>? = null
)

@Root(name = "item", strict = false)
data class BggSearchItem(
    @field:Attribute(name = "id")
    var id: String = "",
    
    @field:Element(name = "name", required = false)
    var name: BggName? = null,
    
    @field:Element(name = "yearpublished", required = false)
    var yearPublished: BggValue? = null
)

@Root(name = "name", strict = false)
data class BggName(
    @field:Attribute(name = "value")
    var value: String = ""
)

@Root(name = "yearpublished", strict = false)
data class BggValue(
    @field:Attribute(name = "value")
    var value: String = ""
)


// Details
@Root(name = "items", strict = false)
data class BggThingResponse(
    @field:ElementList(inline = true, required = false)
    var items: List<BggThingItem>? = null
)

@Root(name = "item", strict = false)
data class BggThingItem(
    @field:Attribute(name = "id")
    var id: String = "",

    @field:Element(name = "thumbnail", required = false)
    var thumbnail: String? = null,

    @field:Element(name = "image", required = false)
    var image: String? = null,
    
    @field:Element(name = "description", required = false)
    var description: String? = null,
    
    @field:ElementList(inline = true, entry = "name", required = false)
    var names: List<BggThingName>? = null,

    @field:Element(name = "yearpublished", required = false)
    var yearPublished: BggValue? = null,
    
    @field:Element(name = "minplayers", required = false)
    var minPlayers: BggValue? = null,

    @field:Element(name = "maxplayers", required = false)
    var maxPlayers: BggValue? = null,

    @field:Element(name = "playingtime", required = false)
    var playingTime: BggValue? = null
)

@Root(name = "name", strict = false)
data class BggThingName(
    @field:Attribute(name = "type")
    var type: String = "",
    
    @field:Attribute(name = "value")
    var value: String = ""
)
