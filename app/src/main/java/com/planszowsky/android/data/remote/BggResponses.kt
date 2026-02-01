package com.planszowsky.android.data.remote

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
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
    var name: BggName? = null,
    
    @field:JacksonXmlProperty(localName = "yearpublished")
    var yearPublished: BggValue? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggName(
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
    var value: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BggThingName(
    @field:JacksonXmlProperty(isAttribute = true)
    var type: String = "",
    
    @field:JacksonXmlProperty(isAttribute = true)
    var value: String = ""
)
