The model processor and data store runtime tests must be located in a different package to ensure we do not implicitly depend on same-package imports and are able to work with consumer package names.
