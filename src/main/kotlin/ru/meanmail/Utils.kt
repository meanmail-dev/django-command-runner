package ru.meanmail

import com.intellij.psi.PsiElement
import com.jetbrains.django.facet.DjangoFacet

fun findDjangoFacet(element: PsiElement): DjangoFacet? {
    return DjangoFacet.getInstance(element)
}
