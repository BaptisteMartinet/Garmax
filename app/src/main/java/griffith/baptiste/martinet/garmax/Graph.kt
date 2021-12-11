package griffith.baptiste.martinet.garmax

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class Graph(context: Context, attrs: AttributeSet) : View(context, attrs) {
  class GraphRange(private var _min: Float = 0f, private var _max: Float = 0f) {
    private var _length = _max - _min
    fun min(): Float = _min
    fun max(): Float = _max
    fun length(): Float = _length
    fun set(min: Float, max: Float) {
      _min = min
      _max = max
      _length = max - min
    }
    fun isInRange(value: Float): Boolean = value in _min.._max
  }

  private val _points: MutableList<PointF> = mutableListOf()
  private val _abscissaAxisName : String
  private val _ordinateAxisName: String
  private val _abscissaStep: Float
  private val _ordinateStep: Float
  private val _abscissaRange: GraphRange
  private val _ordinateRange: GraphRange
  private val _drawPoints: Boolean
  private val _drawLines: Boolean

  private val _displaySize = Point()
  private val _graphSize = PointF()
  private val _bottomLeft = PointF()
  private val _topRight = PointF()

  private val _paintAxis = Paint()
  private val _paintAxisText = TextPaint()
  private val _paintStep = Paint()
  private val _paintStepValueText = TextPaint()
  private val _paintPoint = Paint()
  private val _paintLine = Paint()

  init {
    context.theme.obtainStyledAttributes(attrs, R.styleable.Graph, 0, 0).apply {
      try {
        _abscissaAxisName = getString(R.styleable.Graph_axis_abscissa_name) ?: "None"
        _ordinateAxisName = getString(R.styleable.Graph_axis_ordinate_name) ?: "None"
        _abscissaStep = getFloat(R.styleable.Graph_axis_abscissa_step, 1f)
        _ordinateStep = getFloat(R.styleable.Graph_axis_ordinate_step, 1f)
        _paintStep.color = getColor(R.styleable.Graph_axis_step_color, context.getColor(R.color.white))
        _paintStepValueText.color = getColor(R.styleable.Graph_axis_step_value_color, context.getColor(R.color.white))
        _drawPoints = getBoolean(R.styleable.Graph_draw_point, true)
        _drawLines = getBoolean(R.styleable.Graph_draw_lines, true)
        _paintAxis.color = getColor(R.styleable.Graph_axis_color, context.getColor(R.color.white))
        _paintAxis.strokeWidth = getFloat(R.styleable.Graph_axis_stroke_width, 3f)
        _paintAxisText.color = _paintAxis.color
        _abscissaRange = GraphRange(getFloat(R.styleable.Graph_axis_abscissa_range_min, 0f), getFloat(R.styleable.Graph_axis_abscissa_range_max, 10f))
        _ordinateRange = GraphRange(getFloat(R.styleable.Graph_axis_ordinate_range_min, 0f), getFloat(R.styleable.Graph_axis_ordinate_range_max, 10f))
        _paintPoint.color = getColor(R.styleable.Graph_point_color, context.getColor(R.color.white))
        _paintLine.color = getColor(R.styleable.Graph_line_color, context.getColor(R.color.white))
      } finally {
        recycle()
      }
    }
    _paintAxis.strokeCap = Paint.Cap.SQUARE

    _paintStep.strokeWidth = 3f
    _paintStep.strokeCap = Paint.Cap.SQUARE

    _paintAxisText.textAlign = Paint.Align.CENTER

    _paintLine.strokeCap = Paint.Cap.ROUND
    _paintLine.strokeWidth = 6f
    _paintLine.isAntiAlias = true
  }

  private fun percentX(percent: Float): Float = percent / 100f * _displaySize.x
  private fun percentY(percent: Float): Float = percent / 100f * _displaySize.y

  private fun xValueToGraphCoordinates(x: Float): Float = _bottomLeft.x + ((x - _abscissaRange.min()) / _abscissaRange.length()) * _abscissaRange.length() * (_graphSize.x / _abscissaRange.length())

  private fun yValueToGraphCoordinates(y: Float): Float = _bottomLeft.y - ((y - _ordinateRange.min()) / _ordinateRange.length()) * _ordinateRange.length() * (_graphSize.y / _ordinateRange.length())

  private fun pointValueToGraphCoordinates(point: PointF): PointF? {
    if (!(_abscissaRange.isInRange(point.x) && _ordinateRange.isInRange(point.y)))
      return null
    return PointF(xValueToGraphCoordinates(point.x), yValueToGraphCoordinates(point.y))
  }

  private fun drawPoints(canvas: Canvas) {
    var lastPointPos: PointF? = null
    for (point in _points) {
      val pointPos = pointValueToGraphCoordinates(point) ?: continue
      if (_drawPoints) {
        canvas.drawCircle(pointPos.x, pointPos.y, _paintLine.strokeWidth * 1.5f, _paintPoint)
      }
      if (_drawLines && lastPointPos != null)
        canvas.drawLine(lastPointPos.x, lastPointPos.y, pointPos.x, pointPos.y, _paintLine)
      lastPointPos = pointPos
    }
  }

  private fun drawAbscissaAxis(canvas: Canvas) {
    canvas.drawLine(_bottomLeft.x, _bottomLeft.y, _topRight.x, _bottomLeft.y, _paintAxis)
    canvas.drawText(_abscissaAxisName, percentX(50f), percentY(98f), _paintAxisText)

    _paintStepValueText.textAlign = Paint.Align.LEFT
    var i = _abscissaRange.min() + _abscissaStep
    while (i <= _abscissaRange.max()) {
      val x = xValueToGraphCoordinates(i)
      canvas.drawLine(x, _bottomLeft.y, x, _bottomLeft.y - percentX(1f), _paintStep)
      canvas.rotate(90f, x - percentX(1f), _bottomLeft.y + percentY(2f))
      canvas.drawText("%.1f".format(i) , x - percentX(1f), _bottomLeft.y + percentY(2f), _paintStepValueText)
      canvas.rotate(-90f, x - percentX(1f), _bottomLeft.y + percentY(2f))
      i += _abscissaStep
    }
  }

  private fun drawOrdinateAxis(canvas: Canvas) {
    canvas.drawLine(_bottomLeft.x, _bottomLeft.y, _bottomLeft.x, _topRight.y, _paintAxis)
    canvas.drawText(_ordinateAxisName, percentX(10f), percentY(6f), _paintAxisText)

    _paintStepValueText.textAlign = Paint.Align.RIGHT
    var i = _ordinateRange.min() + _ordinateStep
    while (i <= _ordinateRange.max()) {
      val y = yValueToGraphCoordinates(i)
      canvas.drawLine(_bottomLeft.x, y, _bottomLeft.x + percentX(1f), y, _paintStep)
      canvas.drawText("%.1f".format(i) , _bottomLeft.x - 20, y + percentY(1f), _paintStepValueText)
      i += _ordinateStep
    }
  }

  private fun drawAxis(canvas: Canvas) {
    _paintAxisText.textSize = percentX(3.5f)
    _paintStepValueText.textSize = percentX(2.2f)
    drawAbscissaAxis(canvas)
    drawOrdinateAxis(canvas)
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    canvas ?: return
    drawAxis(canvas)
    drawPoints(canvas)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val w: Int = MeasureSpec.getSize(widthMeasureSpec)
    val h: Int = MeasureSpec.getSize(heightMeasureSpec)
    _displaySize.set(w, h)
    _bottomLeft.set(percentX(10f), percentY(80f))
    _topRight.set(percentX(90f), percentY(10f))
    _graphSize.set(_topRight.x - _bottomLeft.x, _bottomLeft.y - _topRight.y)
  }

  fun loadPoints(points: List<PointF>, autoUpdateRanges: Boolean = false) {
    _points.addAll(points)
    // TODO recompute ranges on auto update
    invalidate()
  }

  fun loadPoint(point: PointF, autoUpdateRanges: Boolean = false) {
    _points.add(point)
    // TODO recompute ranges on auto update
    invalidate()
  }
}
