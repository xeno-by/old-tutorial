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

### show[Semantics]

The most detailed prettyprinter is `show[Semantics]` (very similar to `showRaw` with `printIds`, `printTypes`, etc enabled). With `show[Semantics]`, it is possible to see internal semantic attributes of scala.meta trees (or absence thereof). If you're interested in how these things work, consider checking out the [host implementor's guide](https://github.com/scalameta/scalameta/blob/master/docs/hosts.md).

```
c.sources.foreach(source => println(source.show[Semantics]))
```

In the printout below, we can see the structure of the abstract syntax tree (the same as `show[Structure]`), as well as different kinds of ASCII symbols printed next to some tree nodes:

  1. `[]`'s represent denotations, i.e. resolved names. For instance, by following the `[1]` next to `Type.Name("Bar")`, we can see that `Bar` got resolved to `{7}::_empty_#Bar`. To the left from `::` is what we call a prefix, i.e. a type of the class/object that the name is selected from (in this case, it's the singleton type of the enclosing package), whereas to the right is the fully-qualified name (in this case, `Bar` doesn't belong to any named package, so it gets automatically introduced into the empty package, which is internally called `_empty_`).

  1. `{}`'s represent typings, i.e. results of typechecking of given terms and term parameters. Following the `{1}` next to `Ctor.Ref.Name("this")` in Bar's primary constructor, yields `Type.Method(Seq(Seq()), Type.Name("Bar")[1])`, i.e. a type of the method that takes no arguments and returns a Bar.

  1. `<>`'s represent expansions, i.e. desugarings (parser desugarings, typer desugarings, macro expansions, etc). Since nothing gets desugared in the small code snippet that we're working on, we can only see empty angle brackets that denote absense of expansions.

  1. `*`'s represent untyped nodes, i.e. trees that don't have comprehensive semantic information available. In this particular example, no asterisks are printed, because the trees are fully typechecked.

```
Source(Seq(
  Defn.Class(
    Nil, Type.Name("Bar")[1], Nil,
    Ctor.Primary(Nil, Ctor.Ref.Name("this")[2]{1}<>, Nil),
    Template(
      Nil,
      Nil,
      Term.Param(Nil, Name.Anonymous()[3], None, None){2},
      None)),
  Defn.Class(
    Nil, Type.Name("Foo")[4], Nil,
    Ctor.Primary(Nil, Ctor.Ref.Name("this")[5]{3}<>, Nil),
    Template(
      Nil,
      Seq(Ctor.Ref.Name("Bar")[2]{1}<>),
      Term.Param(Nil, Name.Anonymous()[6], None, None){4},
      Some(Seq(
        Defn.Def(
          Nil,
          Term.Name("bar")[7]{5}<>,
          Nil,
          Seq(Seq(Term.Param(Nil, Term.Name("a")[8]{6}<>,
          Some(Type.Name("Int")[9]), None){6})),
          None,
          Term.Name("a")[8]{6}<>)))))))
[1] {7}::_empty_#Bar
[2] {7}::_empty_#Bar.<init>()V
[3] {8}::_empty_#Bar.this
[4] {7}::_empty_#Foo
[5] {7}::_empty_#Foo.<init>()V
[6] {9}::_empty_#Foo.this
[7] {9}::_empty_#Foo.bar(I)I
[8] {0}::_empty_#Foo.bar(I)I.a
[9] {10}::scala#Int
[10] {11}::_empty_
[11] {11}::scala
[12] {0}::_root_
{1} Type.Method(Seq(Seq()), Type.Name("Bar")[1])
{2} Type.Name("Bar")[1]
{3} Type.Method(Seq(Seq()), Type.Name("Foo")[4])
{4} Type.Name("Foo")[4]
{5} Type.Method(Seq(Seq(Term.Param(Nil, Term.Name("a")[8]{6}<>,
      Some(Type.Name("Int")[9]), None){6})), Type.Name("Int")[9])
{6} Type.Name("Int")[9]
{7} Type.Singleton(Term.Name("_empty_")[10]{7}<>)
{8} Type.Singleton(Term.This(Name.Indeterminate("Bar")[1]){2}<>)
{9} Type.Singleton(Term.This(Name.Indeterminate("Foo")[4]){4}<>)
{10} Type.Singleton(Term.Name("scala")[11]{10}<>)
{11} Type.Singleton(Term.Name("_root_")[12]{11}<>)
```