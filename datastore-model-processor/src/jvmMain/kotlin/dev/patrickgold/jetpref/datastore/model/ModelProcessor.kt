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

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    companion object {
        const val PREFERENCE_DATA_QUALIFIED_NAME = "dev.patrickgold.jetpref.datastore.model.PreferenceData"

        val SAFE_SYMBOL_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]*$".toRegex()
    }

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
                }.joinToString(".") { it.escapedSymbolName() }
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
        // TODO: use kotlinpoet
        file.bufferedWriter().use {
            it.appendLine("package $packageName")
            it.appendLine()
            it.appendLine("import $PREFERENCE_DATA_QUALIFIED_NAME")
            it.appendLine("import dev.patrickgold.jetpref.datastore.runtime.PreferenceModelDuplicateKeyException")
            it.appendLine()
            it.appendLine("class ${finalModelName.escapedSymbolName()} : ${abstractModelName.escapedSymbolName()}() {")
            it.appendLine("  override val declaredPreferenceEntries: Map<TypedKey, PreferenceData<*>>")
            it.appendLine("  init {")
            if (preferenceNames.isEmpty()) {
                it.appendLine("    declaredPreferenceEntries = emptyMap()")
            } else {
                val entries = preferenceNames.map { name -> "$name to \"$name\"" }
                it.appendLine("    val entries = listOf(")
                it.appendLine("      ${entries.joinToString(",\n      ")},")
                it.appendLine("    )")
                it.appendLine("    val duplicates = entries")
                it.appendLine("      .groupBy { (entry, _) -> entry.key }")
                it.appendLine("      .filter { it.value.size > 1 }")
                it.appendLine("      .mapValues { (_, duplicates) -> duplicates.map { (_, varName) -> varName } }")
                it.appendLine("    if (duplicates.isNotEmpty()) {")
                it.appendLine("        throw PreferenceModelDuplicateKeyException(\"${modelClassDecl.qualifiedName!!.asString()}\", duplicates)")
                it.appendLine("    }")
                it.appendLine("    declaredPreferenceEntries = entries")
                it.appendLine("      .map { (entry, _) -> entry }")
                it.appendLine("      .associateBy { TypedKey(it.type, it.key) }")
            }
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

    fun String.escapedSymbolName(): String {
        return if (SAFE_SYMBOL_PATTERN.matches(this)) {
            this
        } else {
            "`$this`"
        }
    }
}

class ModelProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModelProcessor(environment)
    }
}
