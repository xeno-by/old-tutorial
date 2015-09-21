import scala.meta._
import scala.meta.tql._

object Test {
  def main(args: Array[String]): Unit = {
    val stream = getClass.getResourceAsStream("Ordering.scala")
    val tree = stream.parse[Source]
    val tree1 = tree.transform {
      case q"..$mods def $name[..$tparams](...$paramss): $tpeopt = $expr" =>
        val (tparams1, evidences) = tparams.transform {
          case tparam"..$mods $name[..$tparams] >: $lo <: $hi <% ..$vbs : ..$cbs" =>
            val paramEvidences = vbs.map(vb => {
              val evidenceName = Term.fresh("ev")
              val evidenceTpe = name match {
                case name: Type.Name =>
                  t"$name => $vb"
                case name: Name.Anonymous =>
                  // NOTE: These type parameters are bugged in scalac, so we bail.
                  val msg = "can't rewrite context-bounded anonymous type parameters"
                  sys.error(s"error at ${name.position}:\n$msg")
              }
              param"implicit $evidenceName: $evidenceTpe"
            })
            val tparam1 = tparam"..$mods $name[..$tparams] >: $lo <: $hi : ..$cbs"
            tparam1 withResult paramEvidences
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