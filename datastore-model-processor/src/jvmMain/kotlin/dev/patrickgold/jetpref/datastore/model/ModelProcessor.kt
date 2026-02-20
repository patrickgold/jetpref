package dev.patrickgold.jetpref.datastore.model

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    companion object {
        const val PREFERENCE_DATA_QUALIFIED_NAME = "dev.patrickgold.jetpref.datastore.model.PreferenceData"
        const val PREFERENCES_QUALIFIED_NAME = "dev.patrickgold.jetpref.datastore.annotations.Preferences"
        const val PREFERENCE_MODEL_DUPLICATE_KEY_EXCEPTION_QUALIFIED_NAME =
            "dev.patrickgold.jetpref.datastore.runtime.PreferenceModelDuplicateKeyException"
        const val TYPED_KEY_QUALIFIED_NAME = "dev.patrickgold.jetpref.datastore.model.PreferenceModel.TypedKey"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (symbols, symbolsToProcessNextRound) = resolver
            .getSymbolsWithAnnotation(PREFERENCES_QUALIFIED_NAME)
            .partition { it.validate() }
        val (modelSymbols, invalidModelSymbols) = symbols
            .filterIsInstance<KSClassDeclaration>()
            .partition { it.isAbstract() }
        if (invalidModelSymbols.isNotEmpty()) {
            invalidModelSymbols.forEach {
                env.logger.error("Symbol '$it' is not abstract!", it)
            }
            return symbolsToProcessNextRound
        }
        modelSymbols.forEach {
            generateModelImplementation(env, it)
        }
        return symbolsToProcessNextRound
    }

    fun collectProperties(modelClassDecl: KSClassDeclaration): List<List<KSPropertyDeclaration>> {
        val results = mutableListOf<List<KSPropertyDeclaration>>()

        fun nestedClassMap(container: KSClassDeclaration) =
            container.declarations
                .filterIsInstance<KSClassDeclaration>()
                .associateBy { it.simpleName.asString() }

        fun recurse(container: KSClassDeclaration, instanceChain: List<KSPropertyDeclaration>) {
            container.declarations
                .filterIsInstance<KSPropertyDeclaration>()
                .filter { isPreferenceData(it.type.resolve().declaration) }
                .forEach { prop ->
                    results += (instanceChain + prop)
                }
            val nested = nestedClassMap(container)
            container.declarations
                .filterIsInstance<KSPropertyDeclaration>()
                .forEach { holderProp ->
                    val declaredTypeName = holderProp.type.resolve().declaration.simpleName.asString()
                    val matched = nested[declaredTypeName]
                    if (matched != null) {
                        recurse(matched, instanceChain + holderProp)
                    }
                }
        }

        modelClassDecl.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .filter { !isInnerClassOf(modelClassDecl, it.type.resolve().declaration) }
            .forEach { prop ->
                if (isPreferenceData(prop.type.resolve().declaration)) {
                    results += listOf(prop)
                }
            }

        modelClassDecl.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .forEach { holderProp ->
                val nested = nestedClassMap(modelClassDecl)
                val declaredTypeName = holderProp.type.resolve().declaration.simpleName.asString()
                val matched = nested[declaredTypeName]
                if (matched != null) recurse(matched, listOf(holderProp))
            }
        return results
    }

    fun generateModelImplementation(env: SymbolProcessorEnvironment, modelClassDecl: KSClassDeclaration) {
        val properties = collectProperties(modelClassDecl)
        val packageName = modelClassDecl.packageName.asString()
        val abstractModelName = modelClassDecl.simpleName.asString()
        val finalModelName = abstractModelName + "Impl"
        val declaredPreferenceEntriesName = "declaredPreferenceEntries"
        val duplicateClassName = ClassName.bestGuess(PREFERENCE_MODEL_DUPLICATE_KEY_EXCEPTION_QUALIFIED_NAME)
        val typedKeyClassName = ClassName.bestGuess(TYPED_KEY_QUALIFIED_NAME)
        val preferenceDataClassName = ClassName.bestGuess(PREFERENCE_DATA_QUALIFIED_NAME)

        val initBlock = buildCodeBlock {
            val entriesName = "entries"
            val duplicatesName = "duplicates"
            if (properties.isEmpty()) {
                add("%N = emptyMap()", declaredPreferenceEntriesName)
            } else {
                add("val %N = listOf(\n", entriesName)
                withIndent {
                    properties.forEach { props ->
                        val first = CodeBlock.builder()
                        val second = CodeBlock.builder()
                        props.forEachIndexed { index, declaration ->
                            fun computeDeclaration(block: CodeBlock.Builder, modifier: String) {
                                val name = declaration.simpleName.getShortName()
                                if (index != props.lastIndex) {
                                    block.add("$modifier.", name)
                                } else {
                                    block.add(modifier, name)
                                }
                            }
                            computeDeclaration(first, "%N")
                            computeDeclaration(second, "%L")
                        }
                        add(first.build())
                        add(" to ")
                        add("%S", second.build())
                        add(",\n")
                    }
                }
                add(")\n")
                add("val %N = %N\n", duplicatesName, entriesName)
                withIndent {
                    add(".%1N { (%2N, _) -> %2N.key }\n", "groupBy", "entry")
                    add(".%1N { it.value.size > 1 }\n", "filter")
                    add(".%1N { (_, %2N) -> %2N.map { (_, %3N) -> %3N } }\n", "mapValues", duplicatesName, "varName")
                }
                controlFlow("if ($duplicatesName.isNotEmpty()) {") {
                    add(
                        "throw %T(%S, %N)\n",
                        duplicateClassName,
                        modelClassDecl.qualifiedName!!.asString(),
                        duplicatesName
                    )
                }
                add("%N = %N\n", declaredPreferenceEntriesName, entriesName)
                withIndent {
                    add(".%1N { (%2N, _) -> %2N }\n", "map", "entry")
                    add(".%1N { %2T(it.type, it.key) }\n", "associateBy", typedKeyClassName)
                }
            }
        }

        val declaredPreferencesEntriesProperty = PropertySpec
            .builder(
                declaredPreferenceEntriesName,
                MAP.parameterizedBy(
                    typedKeyClassName,
                    preferenceDataClassName
                        .parameterizedBy(STAR)
                )
            ).apply {
                addModifiers(KModifier.OVERRIDE)
                addOriginatingKSFile(modelClassDecl.containingFile!!)
            }.build()

        val modelImplClass = TypeSpec
            .classBuilder(finalModelName)
            .apply {
                superclass(modelClassDecl.toClassName())
                addProperty(
                    declaredPreferencesEntriesProperty
                )
                addInitializerBlock(
                    initBlock
                )
                addOriginatingKSFile(modelClassDecl.containingFile!!)
            }.build()

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = finalModelName,
        ).apply {
            addType(
                modelImplClass
            )
            indent("    ")
            addFileComment(
                "%L\n%L\n%L",
                "This file is autogenerated by jetpref.",
                "DO NOT EDIT BY HAND!",
                "ALL EDITS ARE OVERWRITTEN WITH THE NEXT RECOMPILATION!"
            )
        }.build()
        fileSpec.writeTo(env.codeGenerator, aggregating = true)
    }

    fun isPreferenceData(propertyTypeDecl: KSDeclaration): Boolean {
        return propertyTypeDecl.qualifiedName?.asString() == PREFERENCE_DATA_QUALIFIED_NAME
    }

    @OptIn(ExperimentalContracts::class)
    fun isInnerClassOf(modelClassDecl: KSClassDeclaration, decl: KSDeclaration): Boolean {
        contract {
            returns(true) implies (decl is KSClassDeclaration)
        }
        var parent = decl.parent
        while (parent != null) {
            if (parent == modelClassDecl && decl is KSClassDeclaration) {
                return true
            }
            parent = parent.parent
        }
        return false
    }
}

private inline fun CodeBlock.Builder.withIndent(block: CodeBlock.Builder.() -> Unit) = apply {
    indent()
    block()
    unindent()
}

private inline fun CodeBlock.Builder.controlFlow(controlFlow: String, block: CodeBlock.Builder.() -> Unit) = apply {
    beginControlFlow(controlFlow)
    block()
    endControlFlow()
}

class ModelProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModelProcessor(environment)
    }
}
