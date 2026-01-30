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
                <thumbnail>https://picsum.photos/id/10/400/600.jpg</thumbnail>
                <image>https://picsum.photos/id/10/800/1200.jpg</image>
                <name type="primary" sortindex="1" value="Dixit"/>
                <description>Dixit to gra karciana o surrealistycznych ilustracjach, w której gracze używają wyobraźni, by dopasować karty do opowieści narratora.</description>
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
                <thumbnail>https://picsum.photos/id/20/400/400.jpg</thumbnail>
                <image>https://picsum.photos/id/20/800/800.jpg</image>
                <name type="primary" sortindex="1" value="Catan"/>
                <description>W osadnikach z Catanu gracze starają się zdominować wyspę, budując osady, miasta i drogi na bogatym w surowce terenie.</description>
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
                <thumbnail>https://picsum.photos/id/30/400/550.jpg</thumbnail>
                <image>https://picsum.photos/id/30/800/1100.jpg</image>
                <name type="primary" sortindex="1" value="Ticket to Ride"/>
                <description>Elegancja i prostota. Ticket to Ride to wielokrotnie nagradzana przygoda pociągiem przez całą Amerykę Północną.</description>
                <yearpublished value="2004"/>
                <minplayers value="2"/>
                <maxplayers value="5"/>
                <playingtime value="60"/>
            </item>
        </items>
    """.trimIndent()
}