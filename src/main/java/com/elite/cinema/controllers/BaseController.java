package com.elite.cinema.controllers;

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
}
