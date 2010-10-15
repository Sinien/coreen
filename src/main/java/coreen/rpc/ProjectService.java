//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import coreen.model.CompUnit;
import coreen.model.CompUnitDetail;
import coreen.model.Def;
import coreen.model.DefContent;
import coreen.model.DefDetail;
import coreen.model.Project;
import coreen.model.TypeDetail;
import coreen.model.TypeSummary;

/**
 * Provides project-related services.
 */
@RemoteServiceRelativePath(ProjectService.ENTRY_POINT)
public interface ProjectService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "project";

    /** Returns metadata for the specified project.
     * @throws ServiceException with e.no_such_project if project unknown. */
    Project getProject (long id) throws ServiceException;

    /** Requests that the specified project be updated.
     * @throws ServiceException with e.no_such_project if project unknown. */
    void updateProject (long id) throws ServiceException;

    /** Returns all compilation units associated with the specified project. */
    CompUnit[] getCompUnits (long projectId) throws ServiceException;

    /** Returns all modules in the specified project and their immediate members. The results are
     * a 2D array where each subarray is a module def and its members in sorted order. */
    Def[][] getModsAndMembers (long projectId) throws ServiceException;

    /** Returns all modules in the specified project. */
    Def[] getModules (long projectId) throws ServiceException;

    /** Returns all of the types defined by this project. */
    Def[] getTypes (long projectId) throws ServiceException;

    /** Returns all defs that are immediate children of the specified def. */
    Def[] getMembers (long defId) throws ServiceException;

    /** Returns details for the specified compilation unit.
     * @throws ServiceException with e.no_such_unit if the unit is unknown. */
    CompUnitDetail getCompUnit (long unitId) throws ServiceException;

    /** Returns details for the specified definition.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    DefDetail getDef (long defId) throws ServiceException;

    /** Returns the details for the specified type.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    TypeDetail getType (long defId) throws ServiceException;

    /** Returns a summary of the specified type.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    TypeSummary getSummary (long defId) throws ServiceException;

    /** Returns the contents of the specified definition.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    DefContent getContent (long defId) throws ServiceException;

    /** Searches for defs in the specified project that match the specified query. */
    DefDetail[] search (long projectId, String query) throws ServiceException;
}
