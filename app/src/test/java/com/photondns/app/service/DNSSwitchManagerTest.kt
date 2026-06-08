package com.photondns.app.service

import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.DNSProtocol
import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.data.repository.LatencyRepository
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DNSSwitchManagerTest {

    private lateinit var dnsSwitchManager: DNSSwitchManager
    private val dnsServerRepository: DNSServerRepository = mockk()
    private val latencyRepository: LatencyRepository = mockk()
    private val dnsLatencyChecker: DNSLatencyChecker = mockk()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        dnsSwitchManager = DNSSwitchManager(dnsServerRepository, latencyRepository, dnsLatencyChecker)
    }

    @Test
    fun `shouldSwitch returns true when current server is failing and fastest has latency`() = runTest {
        val currentServer = DNSServer(
            id = "server1",
            name = "Current",
            ip = "1.1.1.1",
            countryCode = "US",
            latency = -1,
            isActive = true,
            protocol = DNSProtocol.UDP
        )
        val fastestServer = DNSServer(
            id = "server2",
            name = "Fastest",
            ip = "8.8.8.8",
            countryCode = "US",
            latency = 10,
            isActive = false,
            protocol = DNSProtocol.UDP
        )

        val strategies = com.photondns.app.data.models.SwitchStrategy.getPresets()
        dnsSwitchManager.updateStrategy(strategies[1]) // Balanced
        val result = dnsSwitchManager.shouldSwitch(currentServer, fastestServer)
        assertTrue(result)
    }

    @Test
    fun `shouldSwitch returns false when both servers have same latency as current`() = runTest {
        val currentServer = DNSServer(
            id = "server1",
            name = "Current",
            ip = "1.1.1.1",
            countryCode = "US",
            latency = 20,
            isActive = true,
            protocol = DNSProtocol.UDP
        )
        val fastestServer = DNSServer(
            id = "server2",
            name = "Fastest",
            ip = "8.8.8.8",
            countryCode = "US",
            latency = 20,
            isActive = false,
            protocol = DNSProtocol.UDP
        )

        val strategies = com.photondns.app.data.models.SwitchStrategy.getPresets()
        dnsSwitchManager.updateStrategy(strategies[1]) // Balanced
        val result = dnsSwitchManager.shouldSwitch(currentServer, fastestServer)
        assertFalse(result)
    }

    @Test
    fun `setAutoSwitchEnabled starts monitoring when true`() {
        dnsSwitchManager.setAutoSwitchEnabled(true)
        assertTrue(dnsSwitchManager.autoSwitchEnabled.value)
        dnsSwitchManager.cleanup()
    }

    @Test
    fun `setAutoSwitchEnabled stops monitoring when false`() {
        dnsSwitchManager.setAutoSwitchEnabled(true)
        dnsSwitchManager.setAutoSwitchEnabled(false)
        assertFalse(dnsSwitchManager.autoSwitchEnabled.value)
    }
}