package com.decorator

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView

// region configs
sealed class Config {
    data class OffsetsConfig(
        val location: Location,
        var value: Int? = null,
    ) : Config()

    data class CanvasConfig(
        val location: Location,
        var bitmap: Bitmap? = null,
        var paint: Paint? = null,
    ) : Config()

    data class DividerConfig(
        val location: Location,
        var value: Int = 0,
        @ColorInt var color: Int? = null,
        var marginLeft: Int = 0,
        var marginRight: Int = 0,
    ) : Config()

    data class DividerDrawableConfig(
        val location: Location,
        var drawable: Drawable? = null,
        var marginLeft: Int = 0,
        var marginRight: Int = 0,
    ) : Config()

    data class StickyHeaderConfig(
        var bitmap: Bitmap? = null,
        var paint: Paint? = null,
        var left: Float = 0f,
        var top: Float = 0f,
    ) : Config()

    enum class Location {
        TOP, BOTTOM, LEFT, RIGHT
    }
}
// endregion

// region types
typealias HT = RecyclerView.ViewHolder
// endregion

// region decorator builders
abstract class Decorator: RecyclerView.ItemDecoration() {
    private val _canvasConfigs by lazy {
        mutableListOf<Config.CanvasConfig>()
    }
    private val _dividerConfigs by lazy {
        mutableListOf<Config.DividerConfig>()
    }
    private val _offsetConfigs by lazy {
        mutableListOf<Config.OffsetsConfig>()
    }

    // region private functions
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (_dividerConfigs.isNotEmpty() || _canvasConfigs.isNotEmpty()) {
            c.save()
            if (parent.clipToPadding) {
                c.clipRect(
                    parent.paddingLeft,
                    parent.paddingTop,
                    parent.width - parent.paddingRight,
                    parent.height - parent.paddingBottom
                )
            }

            for (i in 0 until parent.childCount) {
                val view = parent.getChildAt(i)

                val divider = Rect()
                val paint = Paint()
                val viewBounds = Rect()
                parent.getDecoratedBoundsWithMargins(view, viewBounds)

                if (isDecorateDrawOn(view, c, parent, state)) {
                    _dividerConfigs.forEach { dividerConfig ->
                        when (dividerConfig.location) {
                            Config.Location.TOP -> {
                                divider.top = viewBounds.top + dividerConfig.value
                                divider.bottom = viewBounds.top
                                divider.left = viewBounds.left + dividerConfig.marginLeft
                                divider.right = viewBounds.right - dividerConfig.marginRight
                            }
                            Config.Location.BOTTOM -> {
                                divider.top = viewBounds.bottom
                                divider.bottom = viewBounds.bottom - dividerConfig.value
                                divider.left = viewBounds.left + dividerConfig.marginLeft
                                divider.right = viewBounds.right - dividerConfig.marginRight
                            }
                            Config.Location.LEFT -> {
                                divider.top = viewBounds.top + dividerConfig.marginLeft
                                divider.bottom = viewBounds.bottom - dividerConfig.marginRight
                                divider.left = viewBounds.left
                                divider.right = viewBounds.left + dividerConfig.value
                            }
                            Config.Location.RIGHT -> {
                                divider.top = viewBounds.top + dividerConfig.marginLeft
                                divider.bottom = viewBounds.bottom - dividerConfig.marginRight
                                divider.left = viewBounds.right - dividerConfig.value
                                divider.right = viewBounds.right
                            }
                        }

                        paint.color = dividerConfig.color ?: Color.TRANSPARENT

                        c.drawRect(divider, paint)
                    }
                    _canvasConfigs.forEach { canvasConfig ->
                        when(canvasConfig.location) {
                            Config.Location.TOP -> {
                                c.drawBitmap(
                                    canvasConfig.bitmap!!,
                                    viewBounds.left.toFloat(),
                                    viewBounds.top.toFloat(),
                                    canvasConfig.paint
                                )
                            }
                            Config.Location.BOTTOM -> {
                                c.drawBitmap(
                                    canvasConfig.bitmap!!,
                                    viewBounds.left.toFloat(),
                                    viewBounds.bottom.toFloat() - canvasConfig.bitmap!!.height,
                                    canvasConfig.paint
                                )
                            }
                            Config.Location.LEFT -> {
                                c.drawBitmap(
                                    canvasConfig.bitmap!!,
                                    viewBounds.left.toFloat(),
                                    viewBounds.top.toFloat(),
                                    canvasConfig.paint
                                )
                            }
                            Config.Location.RIGHT -> {
                                c.drawBitmap(
                                    canvasConfig.bitmap!!,
                                    viewBounds.right.toFloat() - canvasConfig.bitmap!!.width,
                                    viewBounds.top.toFloat(),
                                    canvasConfig.paint
                                )
                            }
                        }
                    }
                }
            }

            c.restore()
        } else
            super.onDraw(c, parent, state)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (isDecorateItemOffsets(outRect, view, parent, state) && (_offsetConfigs.isNotEmpty() || _canvasConfigs.isNotEmpty())) {
            _offsetConfigs.forEach {
                when (it.location) {
                    Config.Location.TOP -> {
                        outRect.top += it.value ?: 0
                    }
                    Config.Location.BOTTOM -> {
                        outRect.bottom += it.value ?: 0
                    }
                    Config.Location.LEFT -> {
                        outRect.left += it.value ?: 0
                    }
                    Config.Location.RIGHT -> {
                        outRect.right += it.value ?: 0
                    }
                }
            }
        }
    }

    protected abstract fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean

//    protected abstract fun isDecorateDrawOver(
//        view: View,
//        c: Canvas,
//        parent: RecyclerView,
//        state: RecyclerView.State
//    ): Boolean

    protected abstract fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean

    protected fun RecyclerView.getType(view: View): Int = getHolder(view).itemViewType

    protected fun RecyclerView.getPosition(view: View): Int = getChildAdapterPosition(view)

    protected fun RecyclerView.getHolder(view: View): RecyclerView.ViewHolder =
        getChildViewHolder(view)

    protected fun RecyclerView.getId(view: View): Long = getChildItemId(view)
    // endregion

    // region builder functions
    fun divider(location: Config.Location, init: Config.DividerConfig.() -> Unit) {
        val divider = Config.DividerConfig(location = location).apply(init)
        _dividerConfigs.add(divider)
        offset(location) { value = divider.value }
    }

    fun dividerDrawable(location: Config.Location, init: Config.DividerDrawableConfig.() -> Unit) {
//        val divider = Config.DividerDrawableConfig(location = location).apply(init)
//        _configs.add(divider)
//        offset(location) {
//            value = when (location) {
//                Config.Location.TOP, Config.Location.BOTTOM -> divider.drawable?.intrinsicHeight
//                Config.Location.LEFT, Config.Location.RIGHT -> divider.drawable?.intrinsicWidth
//            }
//        }
    }

    fun canvas(location: Config.Location, init: Config.CanvasConfig.() -> Unit) {
        val canvas = Config.CanvasConfig(location = location).apply(init)
        _canvasConfigs.add(canvas)
        offset(location) {
            value = when (location) {
                Config.Location.TOP, Config.Location.BOTTOM -> canvas.bitmap?.height
                Config.Location.LEFT, Config.Location.RIGHT -> canvas.bitmap?.width
            }
        }
    }

    fun offset(location: Config.Location, init: Config.OffsetsConfig.() -> Unit) {
        _offsetConfigs.add(Config.OffsetsConfig(location = location).apply(init))
    }

    fun stickyHeader(init: Config.StickyHeaderConfig.() -> Unit) {
//        _configs.add(Config.StickyHeaderConfig().apply(init))
    }
    // endregion
}

class DecoratorQuery: Decorator() {
    private var _query: Query? = null

    fun query(init: Query.() -> Unit) {
        _query = Query().apply(init)
    }

    // region private functions
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) = checkQuery(view, parent, state)

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) = checkQuery(view, parent, state)

    private fun checkQuery(view: View, parent: RecyclerView, state: RecyclerView.State): Boolean {
        _query ?: return false
        val preview = parent.getOrNull(parent.getPosition(view) - 1)
//        return (_query!!.current?.isInclude == true && _query!!.current?.checkElement(view, parent, state) == true) || (_query!!.preview?.isInclude == true && preview != null && _query!!.preview?.checkElement(preview, parent, state) == true)
        return (_query!!.current?.checkElement(view, parent, state) == (_query!!.current?.isInclude == true)) || (preview != null && _query!!.preview?.checkElement(preview, parent, state) == (_query!!.preview?.isInclude == true))
    }

    private fun QueryElement?.checkElement(view: View, parent: RecyclerView, state: RecyclerView.State): Boolean {
        this ?: return false
        if (!hasElements) return false

        if (hasType && parent.getType(view) == type) {
            return true
        }
        if (hasHolder && parent.getHolder(view) == holder) {
            return true
        }
        if (hasId && parent.getId(view) == id) {
            return true
        }
        if (hasPosition && parent.getPosition(view) == position) {
            return true
        }
        if (hasFirst && isFirst!! && parent.getPosition(view) == 0) {
            return true
        }
        if (hasLast && isLast!! && parent.getPosition(view) == state.itemCount - 1) {
            return true
        }
        return false
    }

    private fun ViewGroup.getOrNull(position: Int): View? {
        return if (position in 0 until childCount)
            get(position)
        else
            null
    }
    // endregion

    inner class Query {
        var current: QueryElement? = null
            private set
        var preview: QueryElement? = null
         private set

        fun current(init: QueryElement.() -> Unit) {
            current = QueryElement.Include().apply(init)
        }

        fun preview(init: QueryElement.() -> Unit) {
            preview = QueryElement.Include().apply(init)
        }

        fun currentNot(init: QueryElement.() -> Unit) {
            current = QueryElement.Exclude().apply(init)
        }

        fun previewNot(init: QueryElement.() -> Unit) {
            preview = QueryElement.Exclude().apply(init)
        }
    }

    sealed class QueryElement {
        var type: Int? = null
        var holder: Class<out HT>? = null
        var id: Long? = null
        var position: Int? = null
        var isFirst: Boolean? = null
        var isLast: Boolean? = null

        class Include: QueryElement()
        class Exclude: QueryElement()
    }

    private val QueryElement.isInclude get() = this is QueryElement.Include
    private val QueryElement.hasElements get() = hasType || hasHolder || hasId || hasPosition || hasFirst || hasLast
    private val QueryElement.hasType get() = type != null
    private val QueryElement.hasHolder get() = holder != null
    private val QueryElement.hasId get() = id != null
    private val QueryElement.hasPosition get() = position != null
    private val QueryElement.hasFirst get() = isFirst != null
    private val QueryElement.hasLast get() = isLast != null
}

class DecoratorAll: Decorator() {
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) = true

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = true
}

class DecoratorByTypes(private val _types: Set<Int>): Decorator() {
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _types.contains(parent.getType(view))

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _types.contains(parent.getType(view))
}

class DecoratorByHolders(private val _holders: Set<Class<out HT>>): Decorator() {
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _holders.contains(parent.getHolder(view)::class.java)

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _holders.contains(parent.getHolder(view)::class.java)
}

class DecoratorByIds(private val _ids: Set<Long>): Decorator() {
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _ids.contains(parent.getId(view))

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _ids.contains(parent.getId(view))
}

class DecoratorByPositions(private val _positions: Set<Int>): Decorator() {
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _positions.contains(parent.getPosition(view))

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = _positions.contains(parent.getPosition(view))
}

class DecoratorFirst: Decorator() {
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = parent.getPosition(view) == 0

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = parent.getPosition(view) == 0
}

class DecoratorLast: Decorator() {
    override fun isDecorateDrawOn(
        view: View,
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = parent.getPosition(view) == state.itemCount - 1

    override fun isDecorateItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ): Boolean = parent.getPosition(view) == state.itemCount - 1
}
// endregion

// region decoration ex functions
inline fun RecyclerView.decorateByQuery(init: DecoratorQuery.() -> Unit) {
    addItemDecoration(DecoratorQuery().apply(init))
}

inline fun RecyclerView.decorateAll(init: DecoratorAll.() -> Unit) {
    addItemDecoration(DecoratorAll().apply(init))
}

inline fun RecyclerView.decorateByTypes(vararg types: Int, init: DecoratorByTypes.() -> Unit) {
    addItemDecoration(DecoratorByTypes(types.toSet()).apply(init))
}

inline fun RecyclerView.decorateByHolders(vararg holders: Class<out HT>, init: DecoratorByHolders.() -> Unit) {
    addItemDecoration(DecoratorByHolders(holders.toSet()).apply(init))
}

inline fun RecyclerView.decorateByIds(vararg ids: Long, init: DecoratorByIds.() -> Unit) {
    addItemDecoration(DecoratorByIds(ids.toSet()).apply(init))
}

inline fun RecyclerView.decorateByPositions(vararg positions: Int, init: DecoratorByPositions.() -> Unit) {
    addItemDecoration(DecoratorByPositions(positions.toSet()).apply(init))
}

inline fun RecyclerView.decorateFirst(init: DecoratorFirst.() -> Unit) {
    addItemDecoration(DecoratorFirst().apply(init))
}

inline fun RecyclerView.decorateLast(init: DecoratorLast.() -> Unit) {
    addItemDecoration(DecoratorLast().apply(init))
}
// endregion

// region utils
inline fun BitmapAsCanvas(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888, init: Canvas.() -> Unit): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, config)
    Canvas(bitmap).apply(init)
    return bitmap
}

inline fun AntiAliasPaint() = Paint().apply { flags += Paint.ANTI_ALIAS_FLAG }
// endregion