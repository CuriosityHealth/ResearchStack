package org.researchstack.backbone.interfaces

interface ITaskProvider {
    fun task(identifier: String): ITask?
}