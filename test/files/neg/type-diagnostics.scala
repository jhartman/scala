object SetVsSet {
  case class Calculator[+T](name: String, parameters: Set[String])
  val binding = Map.empty[String, String]
  def f = Calculator("Hello",binding.keySet)
}

object TParamConfusion {
  def strings(xs: List[String]) = xs
  
  def f1[a <% Ordered[a]](x: List[a]) = {
    def f2[b >: List[a] <% Ordered[b]](x: List[a], y: b): Int = {
      def f3(xs: List[a], ys: List[a]) = -1
      y match { case y1: List[a] => f3(x, y1) }
    }
  }
  
  def f2[String](s: String) = strings(List(s))
}