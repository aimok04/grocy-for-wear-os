package de.kauker.unofficial.grocy.models

import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingListEntry

open class ShoppingListEntry()

class ShoppingListTitleEntry(
    title: String?,
    titleId: Int?
) : ShoppingListEntry() {
    constructor(titleId: Int) : this(title = null, titleId = titleId)
    constructor(title: String) : this(title = title, titleId = null)

    var title: String?
    var titleId: Int?

    init {
        this.title = title
        this.titleId = titleId
    }

}

class ShoppingListGrocyItemEntry(
    entry: GrocyShoppingListEntry
) : ShoppingListEntry() {

    var entry: GrocyShoppingListEntry

    init {
        this.entry = entry
    }

}
