### scala.meta tutorial: Advanced dendrology

[![Join the chat at https://gitter.im/scalameta/scalameta](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scalameta/scalameta?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

### Learning about scala.meta internals

scala.meta is designed to completely abstract metaprogrammers from internal implementation details. Quasiquotes take care of hiding complex data structures used to represent syntax trees. Semantic infrastructure is designed to abstract away the differences between untyped and typed trees. Imutability makes the internals robust and predictable.

However, given the current pre-release status of scala.meta, it is sometimes necessary to understand and maybe even exploit the processes that are happening under the covers, and this short guide is aimed at gearing you for that purpose. We won't have any dedicated code in this guide, but will instead piggyback on [/tree/exploring-semantics](https://github.com/scalameta/tutorial/tree/exploring-semantics).

### show[Syntax]

The simplest prettyprinter in scala.meta is `show[Syntax]` that prints the code that the tree represents. This prettyprinter is used to implement `Tree.toString`, so there's no need to call it explicitly - just `tree.toString` should suffice.

```scala
c.sources.foreach(src => println(src.show[Syntax]))
```

```
/** scaladoc for bar */
class Bar

/**
 * Scaladoc for Foo class
 */
class Foo extends Bar {
  def bar(a: Int) = a
}
```

### show[Structure]

Somewhat more complicated to understand is `show[Structure]` (a close analogue of `showRaw` from scala.reflect). Its printout contains copy-pasteable code that can be used to construct or deconstruct the corresponding AST. In order for the code in the printout to compile, you need to `import scala.meta.internal.ast._`.

```scala
c.sources.foreach(src => println(src.show[Structure]))
```

```
Source(Seq(
  Defn.Class(
    Nil, Type.Name("Bar"), Nil,
    Ctor.Primary(Nil, Ctor.Ref.Name("this"), Nil),
    Template(
      Nil,
      Nil,
      Term.Param(Nil, Name.Anonymous(), None, None),
      None)),
  Defn.Class(
    Nil, Type.Name("Foo"), Nil,
    Ctor.Primary(Nil, Ctor.Ref.Name("this"), Nil),
    Template(
      Nil,
      Seq(Ctor.Ref.Name("Bar")),
      Term.Param(Nil, Name.Anonymous(), None, None),
      Some(Seq(
        Defn.Def(
          Nil,
          Term.Name("bar"),
          Nil,
          Seq(Seq(Term.Param(Nil, Term.Name("a"),
          Some(Type.Name("Int")), None))),
          None,
          Term.Name("a"))))))))
```
