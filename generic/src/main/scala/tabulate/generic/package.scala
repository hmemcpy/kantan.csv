package tabulate

import tabulate.ops._
import shapeless._

package object generic {
  // - ADT cell encoding ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit def coproductCellEncoder[H: CellEncoder, T <: Coproduct: CellEncoder]: CellEncoder[H :+: T] =
    CellEncoder((a: H :+: T) => a match {
      case Inl(h) => h.asCsvCell
      case Inr(t) => t.asCsvCell
    })

  implicit val cnilCellEncoder: CellEncoder[CNil] =
    CellEncoder((_: CNil) => sys.error("trying to encode CNil, this should not happen"))

  implicit def adtCellEncoder[A, R <: Coproduct](implicit gen: Generic.Aux[A, R], e: CellEncoder[R]): CellEncoder[A] =
    CellEncoder(a => e.encode(gen.to(a)))



  // - ADT cell decoding ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit def coproductCellDecoder[H: CellDecoder, T <: Coproduct: CellDecoder]: CellDecoder[H :+: T] = CellDecoder(row =>
    CellDecoder[H].decode(row).map(Inl.apply).orElse(CellDecoder[T].decode(row).map(Inr.apply))
  )

  implicit val cnilCellDecoder: CellDecoder[CNil] = CellDecoder(_ => DecodeResult.decodeFailure)

  implicit def adtCellDecoder[A, R <: Coproduct](implicit gen: Generic.Aux[A, R], d: CellDecoder[R]): CellDecoder[A] =
    CellDecoder(row => d.decode(row).map(gen.from))



  // - ADT row encoding ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit def coproductRowEncoder[H: RowEncoder, T <: Coproduct: RowEncoder]: RowEncoder[H :+: T] =
    RowEncoder((a: H :+: T) => a match {
      case Inl(h) => h.asCsvRow
      case Inr(t) => t.asCsvRow
    })

  implicit val cnilRowEncoder: RowEncoder[CNil] =
    RowEncoder((_: CNil) => sys.error("trying to encode CNil, this should not happen"))

  implicit def adtRowEncoder[A, R <: Coproduct](implicit gen: Generic.Aux[A, R], e: RowEncoder[R]): RowEncoder[A] =
    RowEncoder(a => e.encode(gen.to(a)))



  // - ADT row decoding ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit def coproductRowDecoder[H: RowDecoder, T <: Coproduct: RowDecoder]: RowDecoder[H :+: T] = RowDecoder(row =>
    RowDecoder[H].decode(row).map(Inl.apply).orElse(RowDecoder[T].decode(row).map(Inr.apply))
  )

  implicit val cnilRowDecoder: RowDecoder[CNil] = RowDecoder(_ => DecodeResult.decodeFailure)

  implicit def adtRowDecoder[A, R <: Coproduct](implicit gen: Generic.Aux[A, R], d: RowDecoder[R]): RowDecoder[A] =
    RowDecoder(row => d.decode(row).map(gen.from))


  // - Case class row encoding -----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit def hlistRowEncoder[H: CellEncoder, T <: HList: RowEncoder]: RowEncoder[H :: T] = RowEncoder((a: H :: T) => a match {
    case h :: t => h.asCsvCell +: t.asCsvRow
  })

  implicit val hnilRowEncoder: RowEncoder[HNil] = RowEncoder(_ => Seq.empty)

  implicit def caseClassRowEncoder[A, R <: HList](implicit gen: Generic.Aux[A, R], c: RowEncoder[R]): RowEncoder[A] =
    RowEncoder(a => c.encode(gen.to(a)))


  // - Case class row decoding -----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit def hlistDecoder[H: CellDecoder, T <: HList: RowDecoder]: RowDecoder[H :: T] = RowDecoder(row =>
    row.headOption.map(s =>
      for {
        h <- CellDecoder[H].decode(s)
        t <- RowDecoder[T].decode(row.tail)
      } yield h :: t
    ).getOrElse(DecodeResult.decodeFailure))

  implicit val hnilDecoder: RowDecoder[HNil] = RowDecoder(_ => DecodeResult.success(HNil))

  implicit def caseClassDecoder[A, R <: HList](implicit gen: Generic.Aux[A, R], d: RowDecoder[R]): RowDecoder[A] =
    RowDecoder(s => d.decode(s).map(gen.from))



  // - Case class cell encoding ----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // Thanks Travis Brown for that one:
  // http://stackoverflow.com/questions/33563111/deriving-type-class-instances-for-case-classes-with-exactly-one-field
  implicit def caseClassCellEncoder[A, R, H](implicit gen: Generic.Aux[A, R], ev: R <:< (H :: HNil), e: CellEncoder[H]): CellEncoder[A] =
    CellEncoder((a: A) => ev(gen.to(a)) match {
      case h :: t => e.encode(h)
    })


  // - Case class cell decoding ----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /*
  implicit def caseClassCellDecoder[A, R, H](implicit gen: Generic.Aux[A, R], ev: R <:< (H :: HNil), d: CellDecoder[H]): CellDecoder[A] =
    CellDecoder(s => d.decode(s).map(h => gen.from((h :: HNil).asInstanceOf[R])))
    */
}
