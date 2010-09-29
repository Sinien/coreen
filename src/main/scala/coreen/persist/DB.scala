//
// $Id$

package coreen.persist

import java.util.Date

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity

import coreen.model.{Def => JDef}

/** Provides database services. */
trait DBModule {
  /** Defines our database schemas. */
  object _db extends Schema
  {
    /** Provides access to the projects repository. */
    val projects = table[Project]

    /** Provides access to the compilation units repository. */
    val compunits = table[CompUnit]

    /** Maps {@link JDef.Type} elements to a byte that can be used in the DB. */
    val typeToCode = Map(
      // these mappings must never change
      JDef.Type.MODULE -> 1.toByte,
      JDef.Type.TYPE -> 2.toByte,
      JDef.Type.FUNC -> 3.toByte,
      JDef.Type.TERM -> 4.toByte,
      JDef.Type.UNKNOWN -> 255.toByte
    )

    /** Maps a byte code back to a {@link JDef.Type}. */
    val codeToType = typeToCode map { case(x, y) => (y, x) }

    /** Drops all tables and recreates the schema. Annoyingly this is the only sort of "migration"
     * supported by Squeryl. */
    def reinitSchema {
      drop
      create
    }
  }
}

/** Contains project metadata. */
case class Project (
  /** The (human readable) name of this project. */
  name :String,
  /** The path to the root of this project. */
  rootPath :String,
  /** A string identifying the imported version of this project. */
  version :String,
  /** When this project was imported into the library. */
  imported :Long,
  /** When this project was last updated. */
  lastUpdated :Long
) extends KeyedEntity[Long] {
  /* ctor */ { assert(!rootPath.endsWith("/")) }

  /** A unique identifier for this project (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this("", "", "", 0L, 0L)

  override def toString = "[id=" + id + ", name=" + name + ", vers=" + version + "]"
}

/** Contains metadata for a single compilation unit. */
case class CompUnit (
  /** The id of the project to which this compilation unit belongs. */
  projectId :Long,
  /** The path (relative to the project root) to this compilation unit. */
  path :String,
  /** The time at which this compilation unit was last updated. */
  lastUpdated :Long

) extends KeyedEntity[Long] {
  /** A unique identifier for this project (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, "", 0L)

  override def toString = "[id=" + id + ", pid=" + projectId + ", path=" + path + "]"
}

/** Contains metadata for a definition. */
case class Def (
  /** The id of this definition's enclosing definition, or 0 if none. */
  parentId :Long,
  /** The id of this definition's enclosing compunit. */
  unitId :Long,
  /** This definition's (unqualified) name (i.e. Foo not com.bar.Outer.Foo). */
  name :String,
  /** The nature of this definition (function, term, etc.). See {@link JDef.Type}. */
  typ :Byte,
  /** This definition's (type) signature. */
  sig :Option[String],
  /** This definition's documentation. */
  doc :Option[String],
  /** The character offset in the source file of the start of this definition. */
  defStart :Int,
  /** The character offset in the source file of the end of this definition. */
  defEnd :Int,
  /** The character offset in the file at which this definition's body starts. */
  bodyStart :Int,
  /** The character offset in the file at which this definition's body ends. */
  bodyEnd :Int
) extends KeyedEntity[Long] {
  /** A unique identifier for this definition (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, 0L, "", 0, None, None, 0, 0, 0, 0)

  override def toString = "[id=" + id + ", name=" + name + ", type=" + typ + "]"
}
