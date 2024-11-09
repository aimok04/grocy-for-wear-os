package de.kauker.unofficial.grocy.models

import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingListEntry

open class ShoppingListEntry

class ShoppingListTitleEntry(
    var title: String?,
    var titleId: Int?
) : ShoppingListEntry() {
    constructor(titleId: Int) : this(title = null, titleId = titleId)
    constructor(title: String) : this(title = title, titleId = null)
}

class ShoppingListGrocyItemEntry(
    var entry: GrocyShoppingListEntry
) : ShoppingListEntry() {

    init {
        if(this.entry.note == "null") this.entry.note = ""
    }

}
