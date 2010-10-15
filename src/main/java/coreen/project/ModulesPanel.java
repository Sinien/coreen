//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.util.PanelCallback;

/**
 * Displays all modules for a project, and their direct type members.
 */
public class ModulesPanel extends SummaryPanel
{
    public ModulesPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.MDS;
    }

    @Override // from SummaryPanel
    protected void updateContents (long projectId)
    {
        _linker = UsePopup.byModsInProject(_projectId);
        _projsvc.getModules(projectId, new PanelCallback<Def[]>(_contents) {
            public void onSuccess (Def[] modules) {
                initContents(modules);
            }
        });
    }

    protected void initContents (Def[] modules)
    {
        _contents.clear();

        for (final Def mod : modules) {
            _contents.add(new TogglePanel(Value.create(false)) {
                protected Widget createCollapsed () {
                    return Widgets.newLabel(mod.name, _styles.module());
                }
                protected Widget createExpanded () {
                    final FlowPanel defs = new FlowPanel();
                    defs.add(Widgets.newLabel("Loading..."));
                    _projsvc.getMembers(mod.id, new PanelCallback<Def[]>(defs) {
                        public void onSuccess (Def[] members) {
                            defs.clear();
                            addMembers(defs, members);
                        }
                    });
                    return Widgets.newFlowPanel(Widgets.newLabel(mod.name, _styles.module()), defs);
                }
            });
        }
    }

    protected void addMembers (FlowPanel panel, Def[] members)
    {
        for (final Def def : members) {
            Label label = DefUtil.addDef(panel, def, _linker, _defmap);
            label.addStyleName(_rsrc.styles().actionable());
            label.addClickHandler(new ClickHandler() {
                public void onClick (ClickEvent event) {
                    if (_types.get(def.id).get()) {
                        _types.get(def.id).update(false);
                    } else {
                        Link.go(Page.PROJECT, _projectId,
                                ProjectPage.Detail.MDS, def.id);
                    }
                }
            });
        }
        DefUtil.addClear(panel);
        for (final Def def : members) {
            // create and add the summary panel (hidden) and bind its visibility to a value
            TypeSummaryPanel deets = new TypeSummaryPanel(def.id, _defmap, _members, _linker);
            Bindings.bindVisible(_types.get(def.id), deets);
            panel.add(deets);
        }
    }

    protected interface Styles extends CssResource
    {
        String type ();
        String module ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _contents;

    protected UsePopup.Linker _linker;

    protected interface Binder extends UiBinder<Widget, ModulesPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
