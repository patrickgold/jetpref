package dev.patrickgold.jetpref.datastore.model

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ModelGenerator(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    companion object {
        const val PREFERENCE_DATA_QUALIFIED_NAME = "dev.patrickgold.jetpref.datastore.model.PreferenceData"
    }

    //val alreadyProcessedSymbols = mutableListOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (symbols, symbolsToProcessNextRound) = resolver
            .getSymbolsWithAnnotation("dev.patrickgold.jetpref.datastore.annotations.Preferences")
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

    fun generateModelImplementation(env: SymbolProcessorEnvironment, modelClassDecl: KSClassDeclaration) {
        val preferenceNames = mutableListOf<String>()
        val propertiesToVisit = ArrayDeque<Pair<KSPropertyDeclaration, List<String>>>()
        propertiesToVisit.addAll(modelClassDecl.getDeclaredProperties().map { it to emptyList() })
        while (propertiesToVisit.isNotEmpty()) {
            val (property, groupPrefix) = propertiesToVisit.removeFirst()
            val propertyTypeDecl = property.type.resolve().declaration
            if (isPreferenceData(propertyTypeDecl)) {
                val mediumRareName = buildList {
                    addAll(groupPrefix)
                    add(property.simpleName.asString())
                }.joinToString(".")
                preferenceNames.add(mediumRareName)
            } else if (isInnerClassOf(modelClassDecl, propertyTypeDecl)) {
                val groupPrefix = buildList {
                    addAll(groupPrefix)
                    add(property.simpleName.asString())
                }
                propertiesToVisit.addAll(propertyTypeDecl.getDeclaredProperties().map { it to groupPrefix })
            } else {
                // Ignore??
            }
        }
        val packageName = modelClassDecl.packageName.asString()
        val abstractModelName = modelClassDecl.simpleName.asString()
        val finalModelName = abstractModelName + "Impl"
        val file = env.codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
                modelClassDecl.containingFile!!,
            ),
            packageName = packageName,
            fileName = finalModelName,
        )
        file.bufferedWriter().use {
            it.appendLine("package $packageName")
            it.appendLine()
            it.appendLine("import $PREFERENCE_DATA_QUALIFIED_NAME")
            it.appendLine()
            it.appendLine("class $finalModelName : $abstractModelName() {")
            // TODO: registry should be passed to DataStore
            it.appendLine("  val registry: List<PreferenceData<*>>")
            it.appendLine("  init {")
            it.appendLine("    registry = listOf(")
            it.appendLine("      ${preferenceNames.joinToString(",\n      ")},")
            it.appendLine("    )")
            it.appendLine("  }")
            it.appendLine("}")
        }
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

class ModelGeneratorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModelGenerator(environment)
    }
}
