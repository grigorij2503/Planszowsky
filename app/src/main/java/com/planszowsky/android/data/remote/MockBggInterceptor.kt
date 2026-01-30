package com.planszowsky.android.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockBggInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val uri = chain.request().url.toUri().toString()
        val responseString = when {
            uri.contains("search") -> searchResponse
            uri.contains("thing") && uri.contains("id=39856") -> dixitDetails
            uri.contains("thing") && uri.contains("id=13") -> catanDetails
            uri.contains("thing") && uri.contains("id=9209") -> ticketDetails
            else -> ""
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .body(responseString.toResponseBody("application/xml".toMediaTypeOrNull()))
            .addHeader("content-type", "application/xml")
            .build()
    }

    private val searchResponse = """
        <items total="3" termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
            <item type="boardgame" id="39856">
                <name type="primary" value="Dixit"/>
                <yearpublished value="2008"/>
            </item>
            <item type="boardgame" id="13">
                <name type="primary" value="Catan"/>
                <yearpublished value="1995"/>
            </item>
             <item type="boardgame" id="9209">
                <name type="primary" value="Ticket to Ride"/>
                <yearpublished value="2004"/>
            </item>
        </items>
    """.trimIndent()

    private val dixitDetails = """
        <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
            <item type="boardgame" id="39856">
                <thumbnail>https://cf.geekdo-images.com/mO717W4Jg6a7fXjO5lqNuw__thumb/img/N1d5w8tZ8g9YJ1x9jz6e8qGqX5w=/fit-in/200x150/filters:strip_icc()/pic3483909.jpg</thumbnail>
                <image>https://cf.geekdo-images.com/mO717W4Jg6a7fXjO5lqNuw__original/img/N1d5w8tZ8g9YJ1x9jz6e8qGqX5w=/0x0/filters:format(jpeg)/pic3483909.jpg</image>
                <name type="primary" sortindex="1" value="Dixit"/>
                <description>Dixit is a card game created by Jean-Louis Roubira.</description>
                <yearpublished value="2008"/>
                <minplayers value="3"/>
                <maxplayers value="6"/>
                <playingtime value="30"/>
            </item>
        </items>
    """.trimIndent()

    private val catanDetails = """
        <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
            <item type="boardgame" id="13">
                <thumbnail>https://cf.geekdo-images.com/W3Bsga_uLP9kO91gZ7H8yw__thumb/img/8a9HeqF8ig_NjSXdaRyCza57u_8=/fit-in/200x150/filters:strip_icc()/pic2419375.jpg</thumbnail>
                <image>https://cf.geekdo-images.com/W3Bsga_uLP9kO91gZ7H8yw__original/img/8a9HeqF8ig_NjSXdaRyCza57u_8=/0x0/filters:format(jpeg)/pic2419375.jpg</image>
                <name type="primary" sortindex="1" value="Catan"/>
                <description>In Catan (formerly The Settlers of Catan), players try to be the dominant force on the island of Catan by building settlements, cities, and roads.</description>
                <yearpublished value="1995"/>
                <minplayers value="3"/>
                <maxplayers value="4"/>
                <playingtime value="120"/>
            </item>
        </items>
    """.trimIndent()

    private val ticketDetails = """
        <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
            <item type="boardgame" id="9209">
                <thumbnail>https://cf.geekdo-images.com/ZWJg0dCdrWHxVnc0eFqCzQ__thumb/img/MerO0s-5N09mG9103C695Y7Qk5U=/fit-in/200x150/filters:strip_icc()/pic4427513.jpg</thumbnail>
                <image>https://cf.geekdo-images.com/ZWJg0dCdrWHxVnc0eFqCzQ__original/img/MerO0s-5N09mG9103C695Y7Qk5U=/0x0/filters:format(jpeg)/pic4427513.jpg</image>
                <name type="primary" sortindex="1" value="Ticket to Ride"/>
                <description>With elegantly simple gameplay, Ticket to Ride can be learned in under 15 minutes.</description>
                <yearpublished value="2004"/>
                <minplayers value="2"/>
                <maxplayers value="5"/>
                <playingtime value="60"/>
            </item>
        </items>
    """.trimIndent()
}
