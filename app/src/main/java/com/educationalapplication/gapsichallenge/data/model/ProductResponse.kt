package com.educationalapplication.gapsichallenge.data.model

data class ProductsResponse(
    val item: Item
)

data class Item(
    val props: Props
)

data class Props(
    val pageProps: PageProps
)

data class PageProps(
    val initialData: InitialData
)

data class InitialData(
    val searchResult: SearchResult
)

data class SearchResult(
    val itemStacks: List<ItemStack>
)

data class ItemStack(
    val items: List<ProductResp>
)

data class ProductResp(
    val id: String?,
    val name: String?,
    val price: String?,
    val image: String?,
    val imageInfo: ImageInfo?
)

data class PriceInfo(
    val currentPrice: CurrentPrice
)

data class CurrentPrice(
    val price: Double
)

data class ImageInfo(
    val thumbnailUrl: String?
)

fun ProductsResponse.toProducts(): List<Product> {
    return item
        .props
        .pageProps
        .initialData
        .searchResult
        .itemStacks
        .flatMap { it.items }
        .map {
            Product(
                id = it.id?:"No ID",
                title = it.name?:"No name",
                price = it.price?:"No Price",
                thumbnail = it.imageInfo?.thumbnailUrl?:it.image?:
                "https://i5.walmartimages.com/seo/Lenovo-IdeaPad-1-15-6-inch-Windows-Laptop-AMD-Ryzen-3-7320U-8GB-RAM-256GB-SSD-Abyss-Blue_f5b5e169-c871-48ed-96dc-87f359bc232f.fcd32791a09e8d0617df9300bca9655c.png?odnHeight=180&odnWidth=180&odnBg=FFFFFF"
                //puse una por defecto, no me dieron valores por defecto
            )
        }
}
