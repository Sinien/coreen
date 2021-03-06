//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import coreen.model.CompUnit;
import coreen.model.CompUnitDetail;
import coreen.model.Def;
import coreen.model.DefContent;
import coreen.model.DefDetail;
import coreen.model.MemberInfo;
import coreen.model.Project;
import coreen.model.TypeDetail;
import coreen.model.TypeSummary;

/**
 * Provides the asynchronous version of {@link ProjectService}.
 */
public interface ProjectServiceAsync
{
    /**
     * The async version of {@link ProjectService#getType}.
     */
    void getType (long defId, AsyncCallback<TypeDetail> callback);

    /**
     * The async version of {@link ProjectService#search}.
     */
    void search (long projectId, String query, AsyncCallback<DefDetail[]> callback);

    /**
     * The async version of {@link ProjectService#getContent}.
     */
    void getContent (long defId, AsyncCallback<DefContent> callback);

    /**
     * The async version of {@link ProjectService#getProject}.
     */
    void getProject (long id, AsyncCallback<Project> callback);

    /**
     * The async version of {@link ProjectService#updateProject}.
     */
    void updateProject (Project p, AsyncCallback<Void> callback);

    /**
     * The async version of {@link ProjectService#rebuildProject}.
     */
    void rebuildProject (long id, AsyncCallback<Void> callback);

    /**
     * The async version of {@link ProjectService#deleteProject}.
     */
    void deleteProject (long id, AsyncCallback<Void> callback);

    /**
     * The async version of {@link ProjectService#getCompUnits}.
     */
    void getCompUnits (long projectId, AsyncCallback<CompUnit[]> callback);

    /**
     * The async version of {@link ProjectService#getModsAndMembers}.
     */
    void getModsAndMembers (long projectId, AsyncCallback<Def[][]> callback);

    /**
     * The async version of {@link ProjectService#getModules}.
     */
    void getModules (long projectId, AsyncCallback<Def[]> callback);

    /**
     * The async version of {@link ProjectService#getTypes}.
     */
    void getTypes (long projectId, AsyncCallback<Def[]> callback);

    /**
     * The async version of {@link ProjectService#getModsMembers}.
     */
    void getModsMembers (Iterable<Long> modIds, AsyncCallback<Def[]> callback);

    /**
     * The async version of {@link ProjectService#getMembers}.
     */
    void getMembers (long defId, AsyncCallback<Def[]> callback);

    /**
     * The async version of {@link ProjectService#getMemberInfo}.
     */
    void getMemberInfo (long defId, AsyncCallback<MemberInfo> callback);

    /**
     * The async version of {@link ProjectService#getCompUnit}.
     */
    void getCompUnit (long unitId, AsyncCallback<CompUnitDetail> callback);

    /**
     * The async version of {@link ProjectService#getDef}.
     */
    void getDef (long defId, AsyncCallback<DefDetail> callback);

    /**
     * The async version of {@link ProjectService#getSummary}.
     */
    void getSummary (long defId, AsyncCallback<TypeSummary> callback);

    /**
     * The async version of {@link ProjectService#getSuperTypes}.
     */
    void getSuperTypes (long defId, AsyncCallback<Def[][]> callback);

    /**
     * The async version of {@link ProjectService#getSubTypes}.
     */
    void getSubTypes (long defId, AsyncCallback<Def[][]> callback);

    /**
     * The async version of {@link ProjectService#findUses}.
     */
    void findUses (long defId, AsyncCallback<ProjectService.UsesResult[]> callback);
}
