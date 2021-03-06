package de.rki.coronawarnapp.util

import androidx.fragment.app.Fragment
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * ExternalActionHelper test.
 */
class ExternalActionHelperTest {

    /**
     * Test activity called.
     */
    @Test
    fun testCall() {
        val fragment = mockk<Fragment>()
        every { fragment.startActivity(any()) } just Runs
        ExternalActionHelper.call(fragment, "+77777777777")
        verify(exactly = 1) { fragment.startActivity(any()) }
    }
}
