/*
 * Copyright 2026 DevOps Solutions Kamil Kiewisz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.devopssolutions.aicommitall.actions

import java.awt.Cursor
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.event.MouseInputAdapter

internal class ThreeSectionControlInteraction(
    private val component: JComponent,
    private val model: ThreeSectionControlModel,
    private val renderer: ThreeSectionControlRenderer,
    private val activateSection: (AiCommitAllControlSection, InputEvent?) -> Unit,
) {
    fun install() {
        installMouseHandling()
        installKeyboardHandling()
    }

    private fun installMouseHandling() {
        val listener = object : MouseInputAdapter() {
            override fun mouseMoved(event: MouseEvent) {
                updateHover(renderer.sectionAt(component, event.point))
            }

            override fun mouseExited(event: MouseEvent) {
                updateHover(null)
            }

            override fun mousePressed(event: MouseEvent) {
                component.requestFocusInWindow()
            }

            override fun mouseClicked(event: MouseEvent) {
                val section = renderer.sectionAt(component, event.point)
                if (section != null && model.state.runningSection == null) {
                    activateSection(section, event)
                }
            }
        }
        component.addMouseListener(listener)
        component.addMouseMotionListener(listener)
    }

    private fun installKeyboardHandling() {
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, NO_KEY_MODIFIERS), PREVIOUS_SECTION_ACTION)
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, NO_KEY_MODIFIERS), NEXT_SECTION_ACTION)
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, NO_KEY_MODIFIERS), ACTIVATE_SECTION_ACTION)
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, NO_KEY_MODIFIERS), ACTIVATE_SECTION_ACTION)
        component.actionMap.put(
            PREVIOUS_SECTION_ACTION,
            object : AbstractAction() {
                override fun actionPerformed(event: ActionEvent) {
                    moveKeyboardSection(PREVIOUS_SECTION_DIRECTION)
                }
            },
        )
        component.actionMap.put(
            NEXT_SECTION_ACTION,
            object : AbstractAction() {
                override fun actionPerformed(event: ActionEvent) {
                    moveKeyboardSection(NEXT_SECTION_DIRECTION)
                }
            },
        )
        component.actionMap.put(
            ACTIVATE_SECTION_ACTION,
            object : AbstractAction() {
                override fun actionPerformed(event: ActionEvent) {
                    if (model.state.runningSection == null) {
                        activateSection(model.keyboardSection, null)
                    }
                }
            },
        )
    }

    private fun moveKeyboardSection(direction: Int) {
        val enabledSections = controlSections
            .filter { section -> model.state.isSectionEnabled(section) }
        if (enabledSections.isEmpty()) {
            return
        }

        val currentIndex = enabledSections.indexOf(model.keyboardSection).takeIf { index -> index >= 0 } ?: 0
        val nextIndex = (currentIndex + direction).floorMod(enabledSections.size)
        model.keyboardSection = enabledSections[nextIndex]
        updateHover(model.keyboardSection)
    }

    private fun updateHover(section: AiCommitAllControlSection?) {
        val nextHover = section?.takeIf { candidate -> model.state.isSectionEnabled(candidate) }
        if (model.hoverSection != nextHover) {
            model.hoverSection = nextHover
            component.cursor = if (nextHover == null) {
                Cursor.getDefaultCursor()
            } else {
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }
            component.repaint()
        }
    }
}
