/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.resources.jersey;

import com.freedomotic.api.API;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("/marketplace/")
@Singleton
@Api(value = "marketplace", description = "Manage plugin installation from marketplace(s)", position = 100)
public class MarketplaceResource {

    MarketPlaceService mps = MarketPlaceService.getInstance();
    ArrayList<IPluginCategory> catList;
    protected final static Injector INJECTOR = Guice.createInjector(new FreedomoticInjector());
    protected final static API api = INJECTOR.getInstance(API.class);

    @GET
    @Path("/providers")
    @ApiOperation(value = "Show the list of registered remote marketplace providers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listProviders() {
        return Response.ok(mps.getProviders()).build();
    }

    @GET
    @Path("/categories")
    @ApiOperation(value = "Download a list of plugin categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listCategories() {
        catList = mps.getCategoryList();
        return Response.ok(catList).build();
    }

    @GET
    @Path("categories/{cat}/plugins")
    @ApiOperation(value = "Download a list of plugin from the speicifed category")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPlugins(
             @ApiParam(value = "name of category to fetch", required = true)
            @PathParam("cat") String cat) {
        if (cat != null && !cat.equals("")) {
            for (IPluginCategory category : catList) {
                if (category.getName().equalsIgnoreCase(cat)) {
                    return Response.ok(category.retrievePluginsInfo()).build();
                }
            }
            throw new ItemNotFoundException();
        }
        return Response.ok(mps.getPackageList()).build();
    }
    
    @POST
    @Path("/install/{url}")
    @ApiOperation(value = "Download and install a plugin")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Plugin installation succeded")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response installPlugin(
            @ApiParam(value = "URL of item to fetch", required = true)
            @PathParam("url") String pluginPath) {
        boolean done;
        PluginsManager plugMgr = api.getPluginManager();
        try {
            done = plugMgr.installBoundle(new URL(pluginPath));
        } catch (MalformedURLException ex) {
            done = false;
        }
        if (done) {
            return Response.accepted().build();
        }
        return Response.serverError().build();
    }

}
