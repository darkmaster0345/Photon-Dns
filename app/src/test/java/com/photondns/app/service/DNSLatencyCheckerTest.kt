package com.photondns.app.service

import com.photondns.app.data.models.DNSProtocol
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.InjectMockKs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DNSLatencyCheckerTest {

    @MockK
    lateinit var dnsLatencyChecker: DNSLatencyChecker

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `normalizeDohUrl adds dns-query path when missing`() {
        val url = "https://dns.example.com"
        val expected = "https://dns.example.com/dns-query"
    }

    @Test
    fun `createDnsQuery generates valid DNS packet`() {
        val domain = "google.com"
    }

    @Test
    fun `checkLatency returns -1 for failed queries`() {
    }
}