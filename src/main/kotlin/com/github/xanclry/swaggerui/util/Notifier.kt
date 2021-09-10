package com.github.xanclry.swaggerui.util

import com.github.xanclry.swaggerui.MyBundle
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class Notifier {
    companion object {
        private val NOTIFICATION_GROUP =
            NotificationGroup("Endpoint Load Config Notification", NotificationDisplayType.BALLOON, true)

        fun notifyProject(project: Project?, content: String, type: NotificationType) {
            NOTIFICATION_GROUP.createNotification(content, type).notify(project)
        }

        fun notifyProjectWithContentBeforeBundleMessage(project: Project?, content: String, key: String, type: NotificationType) {
            val msg = content.plus(MyBundle.message(key))
            notifyProject(project, msg, type)
        }

        fun notifyProjectWithContentAfterBundleMessage(project: Project?, content: String, key: String, type: NotificationType) {
            val msg = MyBundle.message(key).plus(content)
            notifyProject(project, msg, type)
        }

        fun notifyProjectWithMessageFromBundle(project: Project?, key: String, type: NotificationType = NotificationType.INFORMATION) {
            val msg = MyBundle.message(key)
            notifyProject(project, msg, type)
        }
    }
}
