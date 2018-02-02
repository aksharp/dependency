package db

import play.api.db.Database

trait DbImplicits {

  val db: Database

  lazy implicit val usersDao: UsersDao = new UsersDao(db)
  lazy implicit val projectsDao: ProjectsDao = new ProjectsDao(db)
  lazy implicit val tokensDao: TokensDao = new TokensDao(db)
  lazy implicit val syncsDao: SyncsDao = new SyncsDao(db)
  lazy implicit val projectBinariesDao: ProjectBinariesDao = new ProjectBinariesDao(db)
  lazy implicit val projectLibrariesDao: ProjectLibrariesDao = new ProjectLibrariesDao(db)
  lazy implicit val recommendationsDao: RecommendationsDao = new RecommendationsDao(db)
  lazy implicit val librariesDao: LibrariesDao = new LibrariesDao(db)
  lazy implicit val binariesDao: BinariesDao = new BinariesDao(db)
  lazy implicit val resolversDao: ResolversDao = new ResolversDao(db)
  lazy implicit val binaryRecommendationsDao: BinaryRecommendationsDao = new BinaryRecommendationsDao(db)
  lazy implicit val libraryVersionsDao: LibraryVersionsDao = new LibraryVersionsDao(db)
  lazy implicit val libraryRecommendationsDao: LibraryRecommendationsDao = new LibraryRecommendationsDao(db)
  lazy implicit val itemsDao: ItemsDao = new ItemsDao(db)
  lazy implicit val binaryVersionsDao: BinaryVersionsDao = new BinaryVersionsDao(db)
  lazy implicit val subscriptionsDao: SubscriptionsDao = new SubscriptionsDao(db)
  lazy implicit val organizationsDao: OrganizationsDao = new OrganizationsDao(db)
  lazy implicit val userIdentifiersDao: UserIdentifiersDao = new UserIdentifiersDao(db)
  lazy implicit val githubUsersDao: GithubUsersDao = new GithubUsersDao(db)
  lazy implicit val membershipsDao: MembershipsDao = new MembershipsDao(db)

}
