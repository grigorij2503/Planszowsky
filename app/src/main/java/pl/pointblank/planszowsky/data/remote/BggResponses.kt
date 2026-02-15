package pl.pointblank.planszowsky.data.remote

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "items")
data class BggSearchResponse(
    @field:JacksonXmlProperty(localName = "item")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    var items: List<BggSearchItem>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggSearchItem(
    @field:JacksonXmlProperty(isAttribute = true)
    var id: String = "",
    
    @field:JacksonXmlProperty(localName = "name")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    var names: List<BggName>? = null,
    
    @field:JacksonXmlProperty(localName = "yearpublished")
    var yearPublished: BggValue? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggName(
    @field:JacksonXmlProperty(isAttribute = true)
    var type: String = "",
    
    @field:JacksonXmlProperty(isAttribute = true)
    var value: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggValue(
    @field:JacksonXmlProperty(isAttribute = true)
    var value: String = ""
)


// Details
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "items")
data class BggThingResponse(
    @field:JacksonXmlProperty(localName = "item")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    var items: List<BggThingItem>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggThingItem(
    @field:JacksonXmlProperty(isAttribute = true)
    var id: String = "",

    @field:JacksonXmlProperty(localName = "thumbnail")
    var thumbnail: String? = null,

    @field:JacksonXmlProperty(localName = "image")
    var image: String? = null,
    
    @field:JacksonXmlProperty(localName = "description")
    var description: String? = null,
    
    @field:JacksonXmlProperty(localName = "name")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    var names: List<BggThingName>? = null,

    @field:JacksonXmlProperty(localName = "yearpublished")
    var yearPublished: BggValue? = null,
    
    @field:JacksonXmlProperty(localName = "minplayers")
    var minPlayers: BggValue? = null,

    @field:JacksonXmlProperty(localName = "maxplayers")
    var maxPlayers: BggValue? = null,

    @field:JacksonXmlProperty(localName = "playingtime")
    var playingTime: BggValue? = null,

    @field:JacksonXmlProperty(localName = "link")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    var links: List<BggLink>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggLink(
    @field:JacksonXmlProperty(isAttribute = true)
    var type: String = "",
    
    @field:JacksonXmlProperty(isAttribute = true)
    var id: String = "",
    
    @field:JacksonXmlProperty(isAttribute = true)
    var value: String = "",

    @field:JacksonXmlProperty(isAttribute = true)
    var inbound: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggThingName(
    @field:JacksonXmlProperty(isAttribute = true)
    var type: String = "",
    
    @field:JacksonXmlProperty(isAttribute = true)
    var value: String = ""
)

// Collection
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "items")
data class BggCollectionResponse(
    @field:JacksonXmlProperty(localName = "item")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    var items: List<BggCollectionItem>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggCollectionItem(
    @field:JacksonXmlProperty(isAttribute = true, localName = "objectid")
    var id: String = "",

    @field:JacksonXmlProperty(localName = "name")
    var name: String? = null,

    @field:JacksonXmlProperty(localName = "thumbnail")
    var thumbnail: String? = null,

    @field:JacksonXmlProperty(localName = "image")
    var image: String? = null,

    @field:JacksonXmlProperty(localName = "yearpublished")
    var yearPublished: String? = null,

    @field:JacksonXmlProperty(localName = "status")
    var status: BggStatus? = null,

    @field:JacksonXmlProperty(localName = "stats")
    var stats: BggCollectionStats? = null,
    
    @field:JacksonXmlProperty(localName = "comment")
    var comment: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggCollectionStats(
    @field:JacksonXmlProperty(isAttribute = true)
    var minplayers: String? = null,
    
    @field:JacksonXmlProperty(isAttribute = true)
    var maxplayers: String? = null,
    
    @field:JacksonXmlProperty(isAttribute = true)
    var playingtime: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggStatus(
    @field:JacksonXmlProperty(isAttribute = true)
    var own: String = "0",
    
    @field:JacksonXmlProperty(isAttribute = true)
    var wishlist: String = "0"
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "user")
data class BggUserResponse(
    @field:JacksonXmlProperty(isAttribute = true)
    var id: String = "",
    
    @field:JacksonXmlProperty(isAttribute = true)
    var name: String = "",
    
    @field:JacksonXmlProperty(localName = "firstname")
    var firstName: BggValue? = null,
    
    @field:JacksonXmlProperty(localName = "lastname")
    var lastName: BggValue? = null,
    
    @field:JacksonXmlProperty(localName = "avatarlink")
    var avatarLink: BggValue? = null,
    
    @field:JacksonXmlProperty(localName = "yearregistered")
    var yearRegistered: BggValue? = null
)
