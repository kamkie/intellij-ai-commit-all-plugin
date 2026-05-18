package pl.devopssolutions.aicommitall.actions

import org.w3c.dom.Element
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.inputStream
import kotlin.test.Test
import kotlin.test.assertContains
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

    @Test
    fun `commit shortcut action mirrors ide commit shortcut`() {
        val action = pluginAction(AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID)

        assertEquals(
            "pl.devopssolutions.aicommitall.actions.AiCommitAllCommitShortcutAction",
            action.getAttribute("class"),
        )
        assertEquals(IDE_COMMIT_ACTION_ID, action.getAttribute("use-shortcut-of"))
    }

    @Test
    fun `push shortcut action mirrors ide commit and push shortcut`() {
        val action = pluginAction(AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID)

        assertEquals(
            "pl.devopssolutions.aicommitall.actions.AiCommitAllPushShortcutAction",
            action.getAttribute("class"),
        )
        assertEquals(IDE_COMMIT_AND_PUSH_ACTION_ID, action.getAttribute("use-shortcut-of"))
    }

    @Test
    fun `shortcut action promoter is registered`() {
        val actionPromoters = pluginDocument().getElementsByTagName("actionPromoter")
        val implementations = (0 until actionPromoters.length).map { index ->
            (actionPromoters.item(index) as Element).getAttribute("implementation")
        }

        assertEquals(
            listOf("pl.devopssolutions.aicommitall.actions.AiCommitAllShortcutActionPromoter"),
            implementations,
        )
    }

    @Test
    fun `plugin declares required AI Assistant dependency`() {
        val dependencies = pluginDocument().getElementsByTagName("depends")
        val dependencyIds = (0 until dependencies.length).map { index ->
            dependencies.item(index).textContent.trim()
        }

        assertContains(dependencyIds, "com.intellij.ml.llm")
    }

    @Test
    fun `settings configurable is registered`() {
        val configurables = pluginDocument().getElementsByTagName("applicationConfigurable")
        val configurable = (0 until configurables.length)
            .map { index -> configurables.item(index) as Element }
            .single { element ->
                element.getAttribute("id") == "pl.devopssolutions.aicommitall.settings"
            }

        assertEquals("AI Commit All", configurable.getAttribute("displayName"))
        assertEquals(
            "pl.devopssolutions.aicommitall.settings.AiCommitAllConfigurable",
            configurable.getAttribute("instance"),
        )
    }

    private fun pluginAction(actionId: String): Element {
        val document = pluginDocument()
        val actions = document.getElementsByTagName("action")

        for (index in 0 until actions.length) {
            val action = actions.item(index) as Element
            if (action.getAttribute("id") == actionId) {
                return action
            }
        }

        error("Action `$actionId` was not found in plugin.xml.")
    }

    private fun pluginDocument() = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(pluginXml.inputStream())

    private val pluginXml: Path =
        Path.of("src", "main", "resources", "META-INF", "plugin.xml")
}
