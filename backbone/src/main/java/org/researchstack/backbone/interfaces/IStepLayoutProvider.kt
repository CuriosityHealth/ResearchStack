package org.researchstack.backbone.interfaces

interface IStepLayoutProvider {
    val stepLayoutClass: Class<*>?
}

interface INewStepLayoutProvider {
    fun stepLayout(step: IStep): Class<*>?
}