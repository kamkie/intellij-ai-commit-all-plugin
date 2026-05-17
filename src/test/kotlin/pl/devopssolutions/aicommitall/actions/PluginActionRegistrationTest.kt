package pl.devopssolutions.aicommitall.actions

import org.w3c.dom.Element
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.inputStream
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PluginActionRegistrationTest {
    @Test
    fun `three-section control is placed after commit and push`() {
        val action = pluginAction("pl.devopssolutions.aicommitall.actions.ThreeSectionControl")
        val addToGroup = action.getElementsByTagName("add-to-group").item(0) as Element

        assertEquals("Vcs.Commit.PrimaryCommitActions", addToGroup.getAttribute("group-id"))
        assertEquals("after", addToGroup.getAttribute("anchor"))
        assertEquals("Git.Commit.And.Push.Executor", addToGroup.getAttribute("relative-to-action"))
    }

    private fun pluginAction(actionId: String): Element {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(pluginXml.inputStream())
        val actions = document.getElementsByTagName("action")

        for (index in 0 until actions.length) {
            val action = actions.item(index) as Element
            if (action.getAttribute("id") == actionId) {
                return action
            }
        }

        error("Action `$actionId` was not found in plugin.xml.")
    }

    private val pluginXml: Path =
        Path.of("src", "main", "resources", "META-INF", "plugin.xml")
}
