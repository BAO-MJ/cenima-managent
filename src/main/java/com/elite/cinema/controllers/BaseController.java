package com.elite.cinema.controllers;

import com.elite.cinema.utils.SceneManager;

public abstract class BaseController
{
    private SceneManager sceneManager;

    public void setSceneManager(SceneManager sceneManager)
    {
        this.sceneManager = sceneManager;
    }

    protected SceneManager getSceneManager()
    {
        return sceneManager;
    }

    protected void dispose()
    {
        sceneManager = null;
    }

    public void ready()
    {

    }
}
