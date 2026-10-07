package com.mrsora.app

import com.mrsora.app.data.OutcomeKind
import com.mrsora.app.network.BanCheckApi
import com.mrsora.app.network.Reason
import com.mrsora.app.network.ResponseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ResponseParserTest {
    @Test fun empty() { val r = ResponseParser.parse("  "); assertEquals(OutcomeKind.ERROR, r.kind); assertEquals(Reason.EMPTY, r.reason) }
    @Test fun html() { assertEquals(Reason.UNREADABLE, ResponseParser.parse("<html>x</html>").reason) }
    @Test fun invalidJson() { assertEquals(Reason.UNREADABLE, ResponseParser.parse("{abc").reason) }
    @Test fun bannedBoolean() { assertEquals(OutcomeKind.BANNED, ResponseParser.parse("""{"banned":true}""").kind) }
    @Test fun notBannedText() { assertEquals(OutcomeKind.NOT_BANNED, ResponseParser.parse("""{"status":"not banned"}""").kind) }
    @Test fun unknownStaysUnknown() { assertEquals(OutcomeKind.UNKNOWN, ResponseParser.parse("""{"status":"ok"}""").kind) }
    @Test fun conflictIsUnknown() { assertEquals(OutcomeKind.UNKNOWN, ResponseParser.parse("""{"banned":true,"status":"not banned"}""").kind) }
    @Test fun plainText() { assertEquals(OutcomeKind.BANNED, ResponseParser.parse("Number is banned").kind) }
    @Test fun detailsAreReal() { assertEquals(listOf("banned" to "true", "x" to "1"), ResponseParser.parse("""{"banned":true,"x":1}""").details) }
    @Test fun validation() {
        assertNotNull(BanCheckApi.validate("")); assertNotNull(BanCheckApi.validate("abc")); assertNull(BanCheckApi.validate("+221771234567"))
    }
}
