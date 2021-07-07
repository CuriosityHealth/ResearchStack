package org.researchstack.backbone.interfaces

import org.researchstack.backbone.step.Step

interface IStepLayoutProvider {
    fun stepLayout(step: IStep): Class<*>?
}

public class BackwardsCompatibleStepLayoutProvider(): IStepLayoutProvider {
    override fun stepLayout(step: IStep): Class<*>? {
        return (step as? Step)?.stepLayoutClass
    }
}