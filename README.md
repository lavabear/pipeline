# Pipeline
One stop shop for data processing. Built in cron scheduler.

## Parts
- pipeline-core - core logic for commands, defines PipelineDao
- pipeline-db - implements Dao, handles database connections
- pipeline-ws - Javalin server
- pipeline-react - React frontend, uses Auth0

## Why?
The pipeline was built to add lottery numbers for Quick Picks. After writing a few scripts to add historical data I
noticed they usually had the same steps and then focused on adding each step as a new command.
