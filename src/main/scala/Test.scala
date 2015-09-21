import scala.meta._

object Test {
  def main(args: Array[String]): Unit = {
    val stream = getClass.getResourceAsStream("Ordering.scala")
    val tree = stream.parse[Source]
    val tree1 = tree.transform {
      case q"..$mods def $name[..$tparams](...$paramss): $tpeopt = $expr" =>
        var evidences: List[Term.Param] = Nil
        val tparams1 = tparams.map {
          case tparam"..$mods $name[..$tparams] >: $lo <: $hi <% ..$vbs : ..$cbs" =>
            val paramEvidences = vbs.map(vb => {
              val evidenceName = Term.fresh("ev")
              val evidenceTpe = t"$name => $vb"
              param"implicit $evidenceName: $evidenceTpe"
            })
            evidences ++= paramEvidences
            tparam"..$mods $name[..$tparams] >: $lo <: $hi : ..$cbs"
        }
        val paramss1 = {
          if (evidences.isEmpty) paramss
          else {
            def isImplicit(p: Term.Param) = {
              val param"..$mods $_: $_ = $_" = p
              mods.collect{ case mod"implicit" => }.nonEmpty
            }
            val shouldMerge = paramss.nonEmpty && paramss.last.exists(isImplicit)
            if (shouldMerge) paramss.init :+ (paramss.last ++ evidences)
            else paramss :+ evidences
          }
        }
        q"..$mods def $name[..$tparams1](...$paramss1): $tpeopt = $expr"
    }
    println(tree1)
  }
}